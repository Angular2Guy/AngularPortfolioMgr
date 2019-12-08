import { Component, OnInit, AfterViewInit } from '@angular/core';
import { trigger, state, animate, transition, style } from '@angular/animations';
import { Router } from '@angular/router';

@Component({
  selector: 'app-spinner',
  templateUrl: './spinner.component.html',
  styleUrls: ['./spinner.component.scss'],
  animations: [
               trigger( 'showSplash', [
                   state( 'true', style( { opacity: 1 } ) ),
                   state( 'false', style( { opacity: 0 } ) ),
                   transition( '1 => 0', animate( '750ms' ) ),
                   transition( '0 => 1', animate( '750ms' ) )
               ])]  
})
export class SpinnerComponent implements AfterViewInit {
  myState = true;  

  constructor(private router: Router) { }

  ngAfterViewInit(): void {
    setTimeout(() => {
		this.myState = false;
		this.router.navigate(['portfolios']);
	});
  }
}