import cgi

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext import db
from google.appengine.ext.webapp.util import run_wsgi_app
from django.utils import simplejson 

class Bla(db.Model):
  path = db.StringProperty()
  
  def funct(bla):
    query = db.GqlQuery("SELECT * FROM Joh WHERE path = :1", bla.path)

    for q in query:
      q.delete()

class Joh(db.Model):
  path = db.StringProperty()
  data = db.StringProperty()
  
class Advert(db.Model):
  path   = db.StringProperty()
  author = db.UserProperty()
  ttl    = db.DateTimeProperty(auto_now_add=True)
  object = db.TextProperty() #base64
  
  def delmd(self): #delete all metadata of some object
    query = db.GqlQuery("SELECT * FROM MetaData WHERE path = :1", self.path)
    
    for md in query:
      md.delete()

class MetaData(db.Model):
  path   = db.StringProperty()
  keystr = db.StringProperty()
  value  = db.StringProperty()

def auth(self):
    if not users.get_current_user():
        self.error(403)
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.out.write('Not Authenticated')
        return -1
    
    if not users.is_current_user_admin():
        self.error(403)
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.out.write('No Administrator')
        return -1
    
    return 0

def store(json):
  advert = Advert()
  user   = users.get_current_user()

  advert.path   = json[0] #extract path from message
  advert.author = user    #store author
  advert.object = json[2] #extract (base64) object from message
  
  advert.put() #store object in database
  
  for k in json[1].keys():
    metadata        = MetaData(parent=advert)
    metadata.path   = json[0]
    metadata.keystr = k
    metadata.value  = json[1][k]
    metadata.put()

class MainPage(webapp.RequestHandler):
  def post(self):
    body = self.request.body
    json = simplejson.loads(body)
    
    store(json)

application = webapp.WSGIApplication(
                                     [('/func/', MainPage)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()