import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './ui/app/app.component';
import { appConfig } from './ui/app/app.config';

bootstrapApplication(AppComponent, appConfig).catch((err) => console.error(err));
