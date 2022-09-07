import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PortfolioSectorsComponent } from './portfolio-sectors.component';

describe('PortfolioSectorsComponent', () => {
  let component: PortfolioSectorsComponent;
  let fixture: ComponentFixture<PortfolioSectorsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PortfolioSectorsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PortfolioSectorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
