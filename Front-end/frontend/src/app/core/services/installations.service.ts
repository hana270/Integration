import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ConfigServiceService } from './config-service.service';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators'; // Add this import
import { AuthService } from '../authentication/auth.service';

@Injectable({
  providedIn: 'root'
})
export class InstallationsService {
  private apiUrl: string;
  private apiUrlp = 'http://localhost:8087/api/installations';

  constructor(private http: HttpClient,
              private authService: AuthService,
              private configService: ConfigServiceService) 
  {
    this.apiUrl = this.configService.installationsApiUrl;
   }

   // Pour les affectations
   createAffectation(commandeId: number, affectationData: any): Observable<any> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });
  
    // Transform data to match backend DTO
    const requestData = {
      installateurId: affectationData.installateurId,
      commandeId: commandeId,
      dateInstallation: affectationData.dateAffectation,
      heureDebut: affectationData.heureDebut || '08:00:00', // Include seconds
      heureFin: affectationData.heureFin || '17:00:00'     // Include seconds
    };
  
    return this.http.post(
      `${this.apiUrl}/affectations/affecterinstallation/${commandeId}`,
      requestData,
      { headers }
    ).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Erreur lors de l\'affectation';
        if (error.error && typeof error.error === 'string') {
          errorMessage = error.error;
        } else if (error.error && error.error.message) {
          errorMessage = error.error.message;
        }
        return throwError(() => new Error(errorMessage));
      })
    );
  }

   /* createAffectation(commandeId: number, affectationData: any): Observable<any> {
      return this.http.post(
        `${this.apiUrlp}/affectations/affecterinstallation/${commandeId}`, 
        affectationData
      );
    }*/

  getAffectations(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/affectations`);
  }

  // Pour les installateurs
  getInstallateurs(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/installateurs`);
  }

  getInstallateursDisponibles(date: string, zone: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/installateurs/disponibles`, {
      params: { date, zone }
    });
  }

  /*affecterInstallateur(commandeId: number, installateurId: number, date: Date): Observable<any> {
    return this.http.post(`${this.apiUrl}/affectations/affecter-installateur`, {
      installateurId,
      dateInstallation: date.toISOString().split('T')[0]
    });
  }*/
}
