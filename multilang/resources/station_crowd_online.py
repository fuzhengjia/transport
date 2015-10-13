
import numpy
from keras.optimizers import RMSprop
from keras.models import Sequential
from keras.layers.core import Dense, Dropout, Activation
from keras.layers.recurrent import LSTM
from data import load_csv_station_crowd
import csv
from datetime import datetime, date, time



class Station_Crowd_Pred(object):
	def read_scaler(self, scaler_file_name):
		min_max_scaler = {}
		csvfile = open(scaler_file_name,"rb")
		csvreader = csv.reader(csvfile, delimiter=',')
		for row in csvreader:
			feature_min = float(row[1])
			feature_max = float(row[2])
			feature_name = row[0]
			min_max_scaler[feature_name] = [feature_min, feature_max]
		return min_max_scaler

	def __init__(self, _station_id):
		n_in = 3
		n_out = 1
		n_hidden = n_in + 10
		self.seq_length = 10

		station_id = _station_id
		scaler_file_name = "/home/nfs/victor/metro-station-crowd/"+station_id+"/"+station_id+"_station_crowd_scaler.csv"
		model_file_name = "/home/nfs/victor/metro-station-crowd/"+station_id+"/"+station_id+"_station_crowd_model.hdf5"

		print 'Building model...'
		print "input: ", n_in, "hidden: ", n_hidden, "out: ", n_out
		self.model = Sequential()
		self.model.add(LSTM(n_hidden, input_dim=n_in, activation='sigmoid', inner_activation='hard_sigmoid')) # try using a GRU instead, for fun
		self.model.add(Dropout(0.5))
		self.model.add(Dense(n_out,init='uniform'))
		self.model.add(Activation('sigmoid'))
		self.model.load_weights(model_file_name)
		self.model.compile(loss='mean_squared_logarithmic_error', optimizer='rmsprop')
		self.min_max_scaler = self.read_scaler(scaler_file_name)
		self.norm_seq_test_data = numpy.zeros((1, 10, 3), dtype=numpy.float64)

	def get_weekday(self, date_str):
		date_time = datetime.strptime(date_str, "%Y-%m-%d")
		weekday = date(date_time.date().year,date_time.date().month,date_time.date().day).isoweekday()
		return weekday

	def get_hour(self, time_str):
		time = datetime.strptime(time_str, '%H:%M:%S').time()
		hour = time.hour
		minute = time.minute
		time_dec = hour + (float)(minute/100.0)
		return float(time_dec)

	def normalization(self, min_max_scaler, feature_name, value):
		[feature_min,feature_max] = min_max_scaler[feature_name]
		norm_value =  (value - feature_min) / (feature_max - feature_min) 
		return norm_value

	def inverse_normalization(self, min_max_scaler, feature_name, norm_value):
		[feature_min,feature_max] = min_max_scaler[feature_name]
		value = norm_value *(feature_max-feature_min) + feature_min
		return value
 
	def preprocessing(self, input_X):
		date_str = input_X[0]
		time_str = input_X[1]
		value_str = input_X[2]
		weekday = self.get_weekday(date_str)
		hour = self.get_hour(time_str)
		value = float(value_str)
		preprocessed_input_X = numpy.array([weekday, hour, value])
		return preprocessed_input_X

	def normalizating_data(self, preprocessed_input_X):
		norm_weekday = self.normalization(self.min_max_scaler,"date", preprocessed_input_X[0])
		norm_time = self.normalization(self.min_max_scaler,"time", preprocessed_input_X[1])
		norm_value = self.normalization(self.min_max_scaler,"value", preprocessed_input_X[2])
		norm_input_X = numpy.array([norm_weekday, norm_time, norm_value])
		return norm_input_X

	def cache_sequence(self, norm_seq_test_data, norm_input_X):
		norm_seq_test_data = numpy.delete(norm_seq_test_data, 9,  axis=1)
		norm_seq_test_data = numpy.insert(norm_seq_test_data,0, norm_input_X, axis=1)
		return norm_seq_test_data

	def get_prediction(self, input_X):
		date_str = input_X[0]
		time_str = input_X[1]

		preprocessed_input_X = self.preprocessing(input_X)

		norm_input_X = self.normalizating_data(preprocessed_input_X)
		
		self.norm_seq_test_data = self.cache_sequence(self.norm_seq_test_data, norm_input_X)
		
		print self.norm_seq_test_data

		norm_pred_label = (self.model.predict(self.norm_seq_test_data))[0][0]

		pred_label = self.inverse_normalization(self.min_max_scaler, "label", norm_pred_label)
		result = [str(date_str)+","+str(time_str),int(pred_label)]
		
		return result
	    # ld = load_csv_station_crowd.Load_Data()
	    # norm_seq_train_data, norm_seq_test_data, norm_seq_train_label, norm_seq_test_label, test_dataset_date_list, test_dataset_time_list,  data_min_max_scaler, label_min_max_scaler = ld.load_dataset(train_file_name, test_file_name)


station_id = "243"
scp = Station_Crowd_Pred(station_id)

x = ["2015-04-24","05:05:00",1]
ret = scp.get_prediction(x)
print ret

x = ["2015-04-24","05:10:00",2]
ret = scp.get_prediction(x)
print ret


x = ["2015-04-24","05:15:00",1]
ret = scp.get_prediction(x)
print ret

x = ["2015-04-24","05:20:00",2]
ret = scp.get_prediction(x)
print ret

x = ["2015-04-24","05:25:00",8]
ret = scp.get_prediction(x)
print ret

x = ["2015-04-24","05:30:00",16]
ret = scp.get_prediction(x)
print ret

x = ["2015-04-24","05:35:00",18]
ret = scp.get_prediction(x)
print ret

x = ["2015-04-24","05:40:00",17]
ret = scp.get_prediction(x)
print ret

x = ["2015-04-24","05:45:00",20]
ret = scp.get_prediction(x)
print ret

x = ["2015-04-24","05:50:00",18]
ret = scp.get_prediction(x)
print ret
