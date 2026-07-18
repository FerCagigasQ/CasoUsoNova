import { Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { ExportReadyEvent, GuaranteeChangeEvent } from '../models/guarantee-event.model';

@Injectable({ providedIn: 'root' })
export class GuaranteeEventsService {
  private readonly eventsUrl = '/api/v1/guarantees/events';

  constructor(private zone: NgZone) {}

  changes(): Observable<GuaranteeChangeEvent> {
    return new Observable<GuaranteeChangeEvent>(observer => {
      const eventSource = new EventSource(this.eventsUrl);

      eventSource.addEventListener('guarantee-change', event => {
        const messageEvent = event as MessageEvent<string>;
        this.zone.run(() => observer.next(JSON.parse(messageEvent.data) as GuaranteeChangeEvent));
      });

      eventSource.onerror = error => {
        this.zone.run(() => observer.error(error));
        eventSource.close();
      };

      return () => eventSource.close();
    });
  }

  exportReady(): Observable<ExportReadyEvent> {
    return new Observable<ExportReadyEvent>(observer => {
      const eventSource = new EventSource(this.eventsUrl);

      eventSource.addEventListener('export-ready', event => {
        const messageEvent = event as MessageEvent<string>;
        this.zone.run(() => observer.next(JSON.parse(messageEvent.data) as ExportReadyEvent));
      });

      eventSource.onerror = error => {
        this.zone.run(() => observer.error(error));
        eventSource.close();
      };

      return () => eventSource.close();
    });
  }
}
