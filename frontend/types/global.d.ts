// Types for compiled templates
declare module 'brn/templates/*' {
  import { TemplateFactory } from 'htmlbars-inline-precompile';
  const tmpl: TemplateFactory;
  export default tmpl;
}

declare module 'ember-inflector' {
  export function pluralize(word: string): string;
  export function singularize(word: string): string;
  interface InflectorInstance {
    uncountable(word: string): void;
    irregular(singular: string, plural: string): void;
  }
  const Inflector: {
    inflector: InflectorInstance;
  };
  export default Inflector;
}

declare module 'tracked-toolbox' {
  export function cached(target: any, key: string, desc: PropertyDescriptor): PropertyDescriptor;
  export function localCopy(target: any, key: string, desc: PropertyDescriptor): PropertyDescriptor;
  export function trackedReset(options?: any): PropertyDecorator;
}

declare module 'ember-ref-bucket' {
  export function ref(name: string): PropertyDecorator;
  export function trackedRef(name: string): PropertyDecorator;
  export function globalRef(name: string): PropertyDecorator;
  export function bucketFor(owner: any): any;
}

declare module 'ember-simple-auth/authenticators/base' {
  import EmberObject from '@ember/object';
  export default class BaseAuthenticator extends EmberObject {
    authenticate(...args: any[]): Promise<any>;
    invalidate(data: any): Promise<any>;
    restore(data: any): Promise<any>;
  }
}

declare module 'ember-simple-auth/services/session' {
  import Service from '@ember/service';
  export default class SessionService extends Service {
    isAuthenticated: boolean;
    data: { authenticated: any };
    authenticate(authenticator: string, ...args: any[]): Promise<any>;
    invalidate(): Promise<void>;
    requireAuthentication(transition: any, routeOrCallback: string | (() => void)): boolean;
    prohibitAuthentication(routeOrCallback: string | (() => void)): boolean;
  }
}

declare module 'ember-simple-auth/mixins/authenticated-route-mixin' {
  import Mixin from '@ember/object/mixin';
  const AuthenticatedRouteMixin: Mixin;
  export default AuthenticatedRouteMixin;
}

declare module 'ember-simple-auth/mixins/unauthenticated-route-mixin' {
  import Mixin from '@ember/object/mixin';
  const UnauthenticatedRouteMixin: Mixin;
  export default UnauthenticatedRouteMixin;
}

declare module 'ember-simple-auth/authenticators/oauth2-password-grant' {
  import BaseAuthenticator from 'ember-simple-auth/authenticators/base';
  export default class OAuth2PasswordGrant extends BaseAuthenticator {
    serverTokenEndpoint: string;
    clientId: string | null;
    refreshAccessTokens: boolean;
    makeRequest(url: string, data: Record<string, string>, headers?: Record<string, string>): Promise<Record<string, unknown>>;
  }
}

declare module 'ember-component-css/pod-names' {
  const podNames: Record<string, string>;
  export default podNames;
}

declare module 'ember-macro-helpers/computed' {
  export default function computed(...args: [...deps: string[], fn: (...values: unknown[]) => unknown]): unknown;
}

declare module 'fetch' {
  const fetch: typeof globalThis.fetch;
  export default fetch;
}

declare module 'sinon' {
  const sinon: any;
  export default sinon;
  export function stub(...args: any[]): any;
  export function spy(...args: any[]): any;
  export function mock(...args: any[]): any;
  export function useFakeTimers(...args: any[]): any;
}
