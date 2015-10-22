#2015-10-20 15:00

import numpy
from keras.preprocessing import sequence
from keras.optimizers import SGD, RMSprop, Adagrad
from keras.utils import np_utils
from keras.models import Sequential
from keras.layers.core import Dense, Dropout, Activation, TimeDistributedDense
from keras.layers.embeddings import Embedding
from keras.layers.recurrent import LSTM, GRU, SimpleDeepRNN, SimpleRNN
import csv
from datetime import datetime, date, time, timedelta
import copy
import os, sys



class Station_flow_Waiting_Pred(object):
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
        n_hidden_1 =  n_in + 15
        n_out = 2

        self.seq_length = 10

        station_id = _station_id
        scaler_file_name = "/home/nfs/data/metro-train-crowd/"+station_id+"/"+station_id+"_train_crowd_scaler.csv"
        model_file_name = "/home/nfs/data/metro-train-crowd/"+station_id+"/"+station_id+"_train_crowd_model.hdf5"

        self.model = Sequential()
        self.model.add(LSTM(output_dim = n_hidden_1,  input_dim=n_in, init='glorot_uniform', inner_init='orthogonal', activation='tanh'))
        self.model.add(Dense(n_out, init='normal', activation='tanh'))
        sgd = SGD(lr=0.2, momentum=0.9, decay=0, nesterov=False)
        self.model.compile(loss='mse', optimizer='sgd')
        self.model.load_weights(model_file_name)

        self.min_max_scaler = self.read_scaler(scaler_file_name)
        self.norm_seq_test_data = numpy.zeros((1, 10, 3), dtype=numpy.float64)


    def get_weekday(self, date_str):
        date_time = datetime.strptime(date_str, "%Y-%m-%d")
        weekday = date(date_time.date().year,date_time.date().month,date_time.date().day).isoweekday()
        return weekday

    def get_hour(self, time_str):
        time = datetime.strptime(time_str, '%H:%M').time()
        hour = time.hour
        minute = time.minute
        time_dec = hour + (float)(minute/100.0)
        return float(time_dec)

    def normalization(self, min_max_scaler, feature_name, value):
        [feature_min,feature_max] = min_max_scaler[feature_name]
        norm_value =  abs(value - feature_min) / abs(feature_max - feature_min)
        return norm_value

    def inverse_normalization(self, min_max_scaler, feature_name, norm_value):
        [feature_min,feature_max] = min_max_scaler[feature_name]
        value = norm_value *abs(feature_max-feature_min) + feature_min
        return value

    def preprocessing(self, input_X):
        [date_str,time_str] = input_X[0].split(",")
        value_str = input_X[1]
        weekday = self.get_weekday(date_str)
        hour = self.get_hour(time_str)
        value = float(value_str)
        preprocessed_input_X = numpy.array([weekday, hour, value])
        return preprocessed_input_X, date_str+","+time_str, value_str

    def normalizating_data(self, preprocessed_input_X):
        norm_weekday = self.normalization(self.min_max_scaler,"date", preprocessed_input_X[0])
        norm_time = self.normalization(self.min_max_scaler,"time", preprocessed_input_X[1])
        norm_value = self.normalization(self.min_max_scaler,"value_passenger_flow_in", preprocessed_input_X[2])
        norm_input_X = numpy.array([norm_weekday, norm_time, norm_value])
        return norm_input_X

    def inverse_normalizating_data(self, norm_result):
        value_station_waiting_up = self.inverse_normalization(self.min_max_scaler,"label_train_crowd_up", norm_result[0])
        value_station_waiting_down = self.inverse_normalization(self.min_max_scaler,"label_train_crowd_down", norm_result[1])
        result = numpy.array([value_station_waiting_up, value_station_waiting_down])
        return result

    def cache_sequence(self, norm_seq_test_data, norm_input_X):
        norm_seq_test_data = numpy.delete(norm_seq_test_data, 9,  axis=1)
        norm_seq_test_data = numpy.insert(norm_seq_test_data,0, norm_input_X, axis=1)
        ##print norm_seq_test_data
        return norm_seq_test_data


    def increase_timestep(self, date_time_str):
        date_time = datetime.strptime(date_time_str, '%Y-%m-%d,%H:%M')
        next_date_time = date_time + timedelta(minutes=5)
        next_date_time_str = str(next_date_time)
        next_date_time_str = next_date_time_str[:-3]
        next_date_time_str = next_date_time_str.replace(" ", ",")
        return next_date_time_str

    def add_random(self,input_X):
        date_time_str = input_X[0]
        value = int(input_X[1])
        rand_value = numpy.random.randint(low=-50, high=50, size=1)
        ##print "rand_value", rand_value
        if value + rand_value < 1:
            value = value - rand_value
        else:
            value = value + rand_value

        return [date_time_str, long(value)]


    def one_step_prediction(self,input_X, norm_seq_test_data):
        preprocessed_input_X, date_time_str, value_str = self.preprocessing(input_X)

        ##print preprocessed_input_X

        norm_input_X = self.normalizating_data(preprocessed_input_X)

        ##print norm_input_X

        norm_seq_test_data = self.cache_sequence(norm_seq_test_data, norm_input_X)

        ##print norm_seq_test_data

        norm_pred_label = (self.model.predict(norm_seq_test_data))[0]

        ##print norm_pred_label

        pred_label = self.inverse_normalizating_data(norm_pred_label)
        pred_label = pred_label.astype("int")
        ##print pred_label


        next_date_time_str = self.increase_timestep(date_time_str)

        step_result = [next_date_time_str,abs(pred_label[0])+1, abs(pred_label[1])+1]

        return step_result, norm_seq_test_data




    def get_prediction(self, input_X):
        result = []
        norm_seq_test_data_future = numpy.zeros((1, 10, 3), dtype=numpy.float64)

        ##print "first step"
        ##print "input", input_X
        step_result, self.norm_seq_test_data = self.one_step_prediction(input_X, self.norm_seq_test_data)
        ##print "output", step_result
        result.append(step_result)
        
        norm_seq_test_data_future = copy.copy(self.norm_seq_test_data)

        for i in xrange(15):
            ##print "future step", i
            fake_input_X = copy.copy(input_X)
            fake_input_X[0] = step_result[0]
            fake_input_X = self.add_random(fake_input_X)
            ##print "input", step_result
            step_result, norm_seq_test_data_future = self.one_step_prediction(fake_input_X, norm_seq_test_data_future)
            ##print "output", step_result
            result.append(step_result)
        
            
        return result








# def load_file(file_name):
#     csvfile = open(file_name,"rb")
#     csvreader = csv.reader(csvfile, delimiter=',')
#     header = next(csvreader)
#     col_to_select = [0,1,3]
#     dataset = numpy.genfromtxt(csvfile, delimiter=",", usecols = (col_to_select),  dtype='str')
#     return dataset

# station_id = str(sys.argv[1])

# path = "/home/victorliang/Research/SODA_2015/model_data/metro_station_flow_model/"+station_id+"/"
# test_file_name = path +"online/"+station_id+"_station_flow_test.csv"
# dataset = load_file(test_file_name)

# sdp = Station_flow_Waiting_Pred(station_id)

# for i in xrange(len(dataset)):
#     date_str = dataset[i,0]
#     time_str = dataset[i,1]
#     time_str = time_str[:-3]
#     value_str = dataset[i,2]

#     inputX = [date_str + "," + time_str, value_str]
#     #print "Input: ", inputX
#     result = sdp.get_prediction(inputX)
#     #print "Output: ", result
#     raw_input("Press Enter to Continue. . .: ")
