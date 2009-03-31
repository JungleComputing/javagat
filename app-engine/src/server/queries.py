import cgi

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db
from django.utils import simplejson 


class Bin(db.Model):
  path = db.StringProperty()
  val  = db.StringProperty()
  data = db.StringProperty()
 
class MainPage(webapp.RequestHandler):
  def get(self):
    self.response.out.write("""
      <html>
      <body>
      This is a test application for queries!<br />
      </body>
      </html>""")

class Load(webapp.RequestHandler):
  def get(self):
    bin = Bin()
    
    bin.path = 'abc'
    bin.val  = 'key1'
    bin.data = 'val1'
    bin.put()
    
    bin = Bin()
    bin.path = 'abc'
    bin.val  = 'key2'
    bin.data = 'val2'
    bin.put()
    
    bin = Bin()
    bin.path = 'def'
    bin.val  = 'key3'
    bin.data = 'val3'
    bin.put()
    
    self.response.out.write('done.')

class TestQueries(webapp.RequestHandler):
  def get(self):
    query = db.GqlQuery("SELECT * FROM Bin")
    self.response.out.write("SELECT * FROM Bin<br />\n")
    
    fooDict = {}
    
    for bin in query:
       self.response.out.write("Key: %s<br />\n" % bin.val)
       self.response.out.write("Value: %s<br />\n" % bin.data)
       fooDict[bin.path] = bin.data
           
    self.response.out.write(simplejson.dumps(fooDict))
    
    self.response.out.write("\n<br />EOQ<br />\nSTART FIND()<br />\n")
    query = db.GqlQuery("SELECT * FROM Bin WHERE val = :1 AND data = :2", "key1", "val1")
    self.response.out.write("SELECT * FROM Bin WHERE<br />\n")
    
    for bin in query:
      self.response.out.write("Path: %s<br />\n" % bin.path)
      self.response.out.write("Key: %s<br />\n" % bin.val)
      self.response.out.write("Value: %s<br />\n" % bin.data)
      
      query = db.GqlQuery("SELECT * FROM Bin WHERE path = :3 AND val = :1 AND data = :2", "key2", "val2", "abc")
      for bin in query:
        self.response.out.write("Path: %s<br />\n" % bin.path)
        self.response.out.write("Key: %s<br />\n" % bin.val)
        self.response.out.write("Value: %s<br />\n" % bin.data)
      
    return   

application = webapp.WSGIApplication(
                                     [('/queries/', MainPage),
                                      ('/queries/load', Load),
                                      ('/queries/test', TestQueries)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()