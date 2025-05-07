import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { OrdersService } from '../../../core/services/orders.service';
import moment from 'moment';
import { AuthService } from '../../../core/authentication/auth.service';
import { forkJoin } from 'rxjs';
import { InstallationsService } from '../../../core/services/installations.service';

@Component({
  selector: 'app-affectation-dialog',
  templateUrl: './affectation-dialog.component.html',
  styleUrls: ['./affectation-dialog.component.css']
})
export class AffectationDialogComponent {
  currentStep = 1;
  installateurs: any[] = [];
  selectedInstallateurs: any[] = [];
  selectedDate: Date | null = null;
  disponibilites: Date[] = [];
  minDate: Date;
  isLoading = false;
  errorMessage: string | null = null;
  commandeDetails: any = null;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dialogRef: MatDialogRef<AffectationDialogComponent>,
    private ordersService: OrdersService,
    private installationsService: InstallationsService,
    private authService: AuthService
  ) {
    this.minDate = new Date();
    this.minDate.setHours(0, 0, 0, 0);
  }

  ngOnInit(): void {
    this.loadInstallateurs();
    this.loadCommandeDetails();
  }

  loadCommandeDetails(): void {
    this.isLoading = true;
    this.ordersService.getCommandeById(this.data.commande.id).subscribe({
      next: (commande) => {
        this.commandeDetails = commande;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des détails:', err);
        this.errorMessage = 'Impossible de charger les détails de la commande';
        this.isLoading = false;
      }
    });
  }

  loadInstallateurs(): void {
    this.isLoading = true;
    this.errorMessage = null;
    
    this.authService.getInstallersCommande().subscribe({
      next: (installateurs) => {
        this.installateurs = installateurs;
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Erreur:', err);
        this.errorMessage = 'Impossible de charger les installateurs';
        this.isLoading = false;
      }
    });
  }

  isSameDate(date1: Date, date2: Date | null): boolean {
    if (!date2) return false;
    return moment(date1).isSame(moment(date2), 'day');
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  nextStep(): void {
    if (this.currentStep === 1) {
      // Validation supplémentaire au cas où
      if (this.selectedInstallateurs && this.selectedInstallateurs.length > 0 && this.selectedDate) {
        this.currentStep = 2;
      }
    } else {
      this.affecter();
    }
  }

  canProceedToNextStep(): boolean {
    if (this.currentStep === 1) {
      // Retourne false si le formulaire n'est pas valide (donce le bouton sera désactivé)
      return !(this.selectedInstallateurs && 
              this.selectedInstallateurs.length > 0 && 
              this.selectedDate && 
              !this.isLoading);
    }
    // Pour l'étape 2, on peut toujours confirmer sauf pendant le chargement
    return this.isLoading;
  }

  affecter(): void {
    if (this.selectedInstallateurs?.length && this.selectedDate) {
      this.isLoading = true;
      
      const affectationData = {
        installateurId: this.selectedInstallateurs[0].userId, // Make sure this matches the property name
        dateAffectation: this.selectedDate.toISOString().split('T')[0],
        heureDebut: '08:00',
        heureFin: '17:00'
      };
  
      this.installationsService.createAffectation(
        this.data.commande.id, 
        affectationData
      ).subscribe({
        next: () => {
          this.dialogRef.close(true);
        },
        error: (err) => {
          console.error('Erreur:', err);
          this.errorMessage = err.message || 'Erreur lors de l\'affectation';
          this.isLoading = false;
        }
      });
    }
  }
}