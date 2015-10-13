import storm
# import station_crowd_online


class CrowdPredicateBoltPython(storm.BasicBolt):
    # def __init__(self):
    #     self.model
    # def prepare(self):
    #     self.model = Station_Crowd_Pred("243")
    #     print "aaa"
    def process(self, tup):
        station = tup.values[0];
        time = tup.values[1];
        value = tup.values[2]
        storm.emit([station,time,value],"update_stream")


bolt = CrowdPredicateBoltPython()
bolt.prepare()
bolt.run()