from django.db import models

class UploadFileModel(models.Model):
    title = models.TextField(default='')
    file = models.FileField(null=True)
