import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ConfigServiceService } from './config-service.service';

@Injectable({
  providedIn: 'root'
})
export class OrdersService {
  //private apiUrl = 'http://localhost:8090/api/panier/commandes'; // Adaptez selon votre configuration
  private apiUrl: string;

  constructor(private http: HttpClient,
              private configService: ConfigServiceService
  ) { 
    this.apiUrl = this.configService.ordersApiUrl + '/commandes';
  }

  getCommandes(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  getCommandeById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/commande/${id}`);
  }

  
}