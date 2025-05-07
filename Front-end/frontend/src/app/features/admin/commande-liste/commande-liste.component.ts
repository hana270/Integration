import { Component, ViewChild } from '@angular/core';
import { CartService } from '../../../core/services/cart.service';
import { AffectationDialogComponent } from '../affectation-dialog/affectation-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { OrdersService } from '../../../core/services/orders.service';
import { MatTableDataSource } from '@angular/material/table';
import { MatTab } from '@angular/material/tabs';
import { MatPaginator } from '@angular/material/paginator';

@Component({
  selector: 'app-commande-liste',
  templateUrl: './commande-liste.component.html',
  styleUrl: './commande-liste.component.css'
})
export class CommandeListeComponent {
  displayedColumns: string[] = ['numeroCommande', 'client', 'statut', 'dateCreation', 'actions'];
  dataSource = new MatTableDataSource<any>();
  isLoading = true;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private ordersService: OrdersService,
    private dialog: MatDialog
  ) { }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

  ngOnInit(): void {
    this.loadCommandes();
  }

  loadCommandes(): void {
    this.isLoading = true;
    this.ordersService.getCommandes().subscribe({
      next: (commandes) => {
        this.dataSource.data = commandes;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des commandes', err);
        this.isLoading = false;
      }
    });
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  getStatusColor(statut: string): string {
    switch (statut) {
      case 'VALIDEE': return 'primary';
      case 'EN_PREPARATION': return 'accent';
      case 'EN_INSTALLATION': return 'warn';
      case 'LIVREE': return 'success';
      case 'ANNULEE': return 'danger';
      default: return '';
    }
  }

  openAffectationDialog(commande: any): void {
    const dialogRef = this.dialog.open(AffectationDialogComponent, {
      width: '600px',
      data: { commande }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadCommandes(); // Rafraîchir la liste si une affectation a été faite
      }
    });
  }
}