from django.db import models

class ShareImage(models.Model):
	title = models.CharField(max_length=100)
	image = models.ImageField(blank=True)
