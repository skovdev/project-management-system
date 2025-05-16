import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {SignUpComponent} from "./component/auth/sign-up/sign-up.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, SignUpComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {

}
