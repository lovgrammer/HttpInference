from django.db import models

class UploadFileModel(models.Model):
    title = models.TextField(default='')
    image = models.ImageField(blank=True)
