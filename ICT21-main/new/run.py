 
from flask import Flask 
from flask import render_template 
import base64
import numpy as np
import io
from PIL import Image
import keras
from keras import backend as k
from keras.models import Sequential
from keras.models import load_model
from keras.preprocessing.image import img_to_array
from keras.preprocessing.image import ImageDataGenerator
from flask import request
from flask import jsonify,make_response
from flask import Flask
import tensorflow as tf
import pandas as pd
import json
#from flask_ngrok import run_with_ngrok

app = Flask(__name__) 
#run_with_ngrok(app)

def get_model():
	global model
	#modelnearby=load_model(name)
	model = load_model('cloud_model_o.h5')
	print('Model Loaded!')


get_model()
global tomato
tomato = tf.get_default_graph()

def preprocess_image(image,target_size):
	if image.mode!="RGB":
		image=image.convert("RGB")
	image=image.resize(target_size)
	image=img_to_array(image)
	image=np.expand_dims(image,axis=0)
	return image





  
@app.route('/') 
@app.route('/home')
def home(): 
    return render_template('home.html')

@app.route('/about')
def about(): 
    return render_template('about.html')

@app.route('/prediction', methods=["POST"])
def predict(): 
	message=request.get_json(force=True)
	encoded=message['image']
	decoded=base64.b64decode(encoded)
	image=Image.open(io.BytesIO(decoded))
	processed_image=preprocess_image(image,target_size=(256,256))
	with tomato.as_default():
		prediction=model.predict(processed_image)
		results = [ prediction[0][i] for i in range(0,15)]
		maxposition = results.index(max(results))
		diseases = ['Pepper Bell Healthy','Pepper Bacterial Spot','Potato Early Blight','Potato Late Blight','Potato Healthy','Tomato Bacterial Spot','Tomato Early Blight','Tomato Late Blight','Tomato Leaf Mold','Tomato Septoria Leaf Spot','Tomato Spider Mite','Tomato Target Spot','Tomato Yellow Leaf Curl Virus','Tomato Mosaic Virus','Tomato Healthy']
		response={
			
			'class' : diseases[maxposition]
			}
	return pd.Series(response).to_json(orient='values')
  

if __name__ == '__main__': 
    app.run(debug=False)