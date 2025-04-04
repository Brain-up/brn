import {
  HttpErrorResponse,
  provideHttpClient,
  withInterceptorsFromDi,
} from "@angular/common/http";
import {
  HttpTestingController,
  provideHttpClientTesting,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { GroupApiService } from "./group-api.service";

const baseUrl = "/api/groups";
const locale = "en";
const id = 1234;

describe("GroupApiService", () => {
  let service: GroupApiService;
  let controller: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [],
      providers: [
        GroupApiService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(GroupApiService);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controller.verify();
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });

  it("should call get groups with locale", () => {
    const url = baseUrl;

    service.getGroups(locale).subscribe((_groups) => {});

    const request = controller.expectOne(`${url}?locale=${locale}`);
    expect(request.request.method).toEqual("GET");
    request.flush("", { status: 204, statusText: "No Data" });
    controller.verify();
  });

  it("should check call errors on get groups with locale", () => {
    const errorEvent = new ErrorEvent("API error");
    const status = 500;
    const statusText = "Server error";
    const url = baseUrl;

    let actualError: HttpErrorResponse | undefined;

    service.getGroups(locale).subscribe(
      () => {
        fail("Next handler must not be called");
      },
      (error) => {
        actualError = error;
      },
      () => {
        fail("Complete handler must not be called");
      }
    );

    controller
      .expectOne(`${url}?locale=${locale}`)
      .error(errorEvent, { status, statusText });

    if (!actualError) {
      throw new Error("Error needs to be defined");
    }
    expect(actualError.error).toBe(errorEvent);
    expect(actualError.status).toBe(status);
    expect(actualError.statusText).toBe(statusText);
  });

  it("should call get group by id", () => {
    const url = `${baseUrl}/${id}`;

    service.getGroupById(id).subscribe((_group) => {});

    const request = controller.expectOne(url);
    expect(request.request.method).toEqual("GET");
    request.flush("", { status: 204, statusText: "No Data" });
    controller.verify();
  });

  it("should check call errors on get group by id", () => {
    const errorEvent = new ErrorEvent("API error");
    const status = 500;
    const statusText = "Server error";
    const url = `${baseUrl}/${id}`;

    let actualError: HttpErrorResponse | undefined;

    service.getGroupById(id).subscribe(
      () => {
        fail("Next handler must not be called");
      },
      (error) => {
        actualError = error;
      },
      () => {
        fail("Complete handler must not be called");
      }
    );

    controller.expectOne(url).error(errorEvent, { status, statusText });

    if (!actualError) {
      throw new Error("Error needs to be defined");
    }
    expect(actualError.error).toBe(errorEvent);
    expect(actualError.status).toBe(status);
    expect(actualError.statusText).toBe(statusText);
  });
});
