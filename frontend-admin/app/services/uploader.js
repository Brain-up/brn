import Service from '@ember/service';
import fetch from 'fetch';

export class GroupModel {
    id = ''
    name = ''
    seriesIds = []
}
  
export class SeriesModel {
    id = ''
    name = ''
    group = ''
    exercises = []
}

function queryParams(params) {
    return Object.keys(params)
        .map(k => encodeURIComponent(k) + '=' + encodeURIComponent(params[k]))
        .join('&');
}

export default class UploaderService extends Service {
    getAuthHeaders() {
        return {
            'Authorization': 'Basic YWRtaW46YWRtaW4='
        }
    }
    getHeaders() {
        return {
            'Content-Type': 'application/json',
        }
    }
    headers() {
        return { ...this.getHeaders(), ...this.getAuthHeaders() };
    }
    apiPrefix = '/api';
    async loadJSON(endpoint, data = {}) {
        const req = await fetch(`${this.apiPrefix}/${endpoint}?${queryParams(data)}`, { headers: this.headers() });
        if (req.ok) {
            return await req.json();
        } else {
            return null;
        }
    }
    async uploadForm(endpoint, form) {
        const req = await fetch (`${this.apiPrefix}/${endpoint}`, {
            method: 'POST',
            headers: this.headers(),
            body: form
        });
        if (req.ok) {
            return await req.json();
        } else {
            return null;
        }
    }
    // '/api/files'
    // '/api/loadTasksFile'
    async getGroups() {
        return await this.loadJSON('groups');
    }
    async getSeriesByGroupId(groupId) {
        return await this.loadJSON('series', { groupId });
    }
    async uploadFiles(file, params  = {}) {
        const formData = new FormData();
        if (params) {
            Object.entries(params).forEach(([key, value]) => formData.append(key, value));
        }
        formData.append('taskFile', file, file.name);
        return formData;
    }
    async uploadTasks() {

    }
}
