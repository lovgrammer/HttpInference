from django.forms import ModelForm
from .models import ShareImage

class ShareImageForm(ModelForm):
    class Meta:
        model = ShareImage
        fields = ['title', 'image']
