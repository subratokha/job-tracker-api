{{- define "postgresApplication.name" -}}
{{ .Release.Name }}-postgres-application
{{- end }}

{{- define "postgresApplication.serviceName" -}}
{{ .Release.Name }}-postgres-application-service
{{- end }}