import dayjs from "dayjs";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { MonthTimeTrackItemComponent } from "./month-time-track-item.component";
import { TranslateModule } from "@ngx-translate/core";

describe("MonthTimeTrackItemComponent", () => {
  let fixture: ComponentFixture<MonthTimeTrackItemComponent>;
  let component: MonthTimeTrackItemComponent;
  let hostElement: HTMLElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot(), MonthTimeTrackItemComponent],
    });

    fixture = TestBed.createComponent(MonthTimeTrackItemComponent);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  describe("Selected class on host", () => {
    beforeEach(() => {
      fixture.componentRef.setInput("data", {
        date: dayjs(),
        days: 23,
        month: "September",
        progress: "BAD",
        time: "02:34:12",
        year: 2021,
      });
      hostElement = fixture.nativeElement;
    });

    it("should NOT has", () => {
      fixture.detectChanges();

      expect(hostElement).not.toHaveClass("selected");
    });

    it("should element have class", () => {
      fixture.componentRef.setInput("isSelected", true);

      fixture.detectChanges();

      expect(hostElement).toHaveClass("selected");
    });
  });
});
