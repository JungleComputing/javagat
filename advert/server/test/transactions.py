import cgi

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext import db
from google.appengine.ext.webapp.util import run_wsgi_app

class Data(db.Model):
  path = db.StringProperty()
  data = db.StringProperty()

class MData(db.Model):
  path = db.StringProperty()
  data = db.IntegerProperty()

def dbfunc():
    data = Data()
    
    data.path = "abc"
    data.data = "some_data"
    
    data.put() #store object in database

    for i in range(1, 5):
      mdata = MData(parent=data)
      mdata.path = "abc"
      mdata.data = i
      mdata.put()
      
    return

class MainPage(webapp.RequestHandler):
  def get(self):
    self.response.out.write('TESTING')
    try:
      db.run_in_transaction(dbfunc)
    except db.TransactionFailedError: 
      self.response.out.write('FAILED')
      return
    
    self.response.out.write('SUCCESS')

application = webapp.WSGIApplication(
                                     [('/trans/', MainPage)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()