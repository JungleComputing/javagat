import cgi

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db
from django.utils import simplejson

class Advert(db.Model):
  path   = db.StringProperty()
  author = db.UserProperty()
  ttl    = db.DateTimeProperty(auto_now_add=True)
  object = db.BlobProperty()
  
class MetaData(db.Model):
  keystr = db.StringProperty()
  value  = db.StringProperty()

class MainPage(webapp.RequestHandler):
   def get(self):
    user = users.get_current_user()
    
    if user:
      self.response.headers['Content-Type'] = 'text/plain'
      self.response.out.write('Hello, ' + user.nickname())
    else:
      self.redirect(users.create_login_url(self.request.uri))
      
class AddObject(webapp.RequestHandler):
  def post(self):
    advert = Advert()
    user   = users.get_current_user()
    
    #if not user:
    #    self.error(403)
    #    self.response.headers['Content-Type'] = 'text/plain'
    #    self.response.out.write('Not Authenticated')
    #    return
    
    #if not users.is_current_user_admin():
    #    self.error(403)
    #    self.response.headers['Content-Type'] = 'text/plain'
    #    self.response.out.write('No Administrator')
    #    return
    
    body = self.request.body
    json = simplejson.loads(body)
    
    advert.path   = json[0] #extract path from message
    advert.author = user    #store author
    advert.object = json[2] #extract (base64) object from message
    
    advert.put() #store object in database
    
    for k in json[1].keys():
      metadata        = MetaData(parent=advert)
      metadata.keystr = k
      metadata.value  = json[1][k]
      metadata.put()
    
    self.response.headers['Content-Type'] = 'text/plain'
    self.response.out.write('TTL') #TODO: insert actual TTL
    return

application = webapp.WSGIApplication(
                                     [('/', MainPage),
                                      ('/add', AddObject)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()