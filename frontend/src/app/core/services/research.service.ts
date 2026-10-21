import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { ResearchResponse } from "../models/research.model";

@Injectable({
    providedIn: 'root'
})
export class ResearchService{
    private readonly http=inject(HttpClient);

    askAgent(question: string): Observable<ResearchResponse> {
        const params=new HttpParams().set('question',question);
        return this.http.get<ResearchResponse>('/api/agent',{params});
    }
}