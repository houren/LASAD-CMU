function z4b(){}
function D4b(){}
function s4b(a,b){a.c=b}
function t4b(a){if(a==i4b){return true}tF();return a==l4b}
function u4b(a){if(a==h4b){return true}tF();return a==g4b}
function B4b(a){this.c=(m6b(),h6b).b;this.e=(t6b(),s6b).b;this.b=a}
function v4b(){m4b();s$b.call(this);this.c=(m6b(),h6b);this.d=(t6b(),s6b);this.f[cHc]=0;this.f[dHc]=0}
function q4b(a,b){var c;c=AU(a.bb,97);c.c=b.b;!!c.d&&(c.d[aHc]=b.b,undefined)}
function r4b(a,b){var c;c=AU(a.bb,97);c.e=b.b;!!c.d&&bXb(c.d,bHc,b.b)}
function m4b(){m4b=cAc;f4b=new z4b;i4b=new z4b;h4b=new z4b;g4b=new z4b;j4b=new z4b;k4b=new z4b;l4b=new z4b}
function n4b(a,b,c){var d;if(c==f4b){if(b==a.b){return}else if(a.b){throw new Dpc('Only one CENTER widget may be added')}}Cj(b);dic(a.k,b);c==f4b&&(a.b=b);d=new B4b(c);b.bb=d;q4b(b,a.c);r4b(b,a.d);p4b(a);Ej(b,a)}
function o4b(a,b){var c,d,e,f,g,i,j;Dhc(a.db,FCc,b);i=new Hxc;j=new oic(a.k);while(j.b<j.c.d-1){c=mic(j);g=AU(c.bb,97).b;e=AU(i.me(g),143);d=!e?1:e.b;f=g==j4b?'north'+d:g==k4b?'south'+d:g==l4b?'west'+d:g==g4b?'east'+d:g==i4b?'linestart'+d:g==h4b?'lineend'+d:QGc;Dhc(Ir(c.db),b,f);i.oe(g,Spc(d+1))}}
function p4b(a){var b,c,d,e,f,g,i,j,k,n,o,p,q,r,s,t;b=a.e;while(pYb(b)>0){mr(b,oYb(b,0))}q=1;e=1;for(i=new oic(a.k);i.b<i.c.d-1;){d=mic(i);f=AU(d.bb,97).b;f==j4b||f==k4b?++q:(f==g4b||f==l4b||f==i4b||f==h4b)&&++e}r=pU(zdb,jAc,98,q,0);for(g=0;g<q;++g){r[g]=new D4b;r[g].c=$doc.createElement($Gc);UWb(b,r[g].c)}k=0;n=e-1;o=0;s=q-1;c=null;for(i=new oic(a.k);i.b<i.c.d-1;){d=mic(i);j=AU(d.bb,97);t=$doc.createElement(_Gc);j.d=t;j.d[aHc]=j.c;bXb(j.d,bHc,j.e);j.d[fDc]=FCc;j.d[dDc]=FCc;if(j.b==j4b){WWb(r[o].c,t,r[o].b);UWb(t,d.db);t[SIc]=n-k+1;++o}else if(j.b==k4b){WWb(r[s].c,t,r[s].b);UWb(t,d.db);t[SIc]=n-k+1;--s}else if(j.b==f4b){c=t}else if(t4b(j.b)){p=r[o];WWb(p.c,t,p.b++);UWb(t,d.db);t[RJc]=s-o+1;++k}else if(u4b(j.b)){p=r[o];WWb(p.c,t,p.b);UWb(t,d.db);t[RJc]=s-o+1;--n}}if(a.b){p=r[o];WWb(p.c,c,p.b);UWb(c,a.b.db)}}
Meb(744,1,ZAc);_.qc=function sCb(){var a,b,c;phb(this.b,(a=new v4b,a.db[eDc]='cw-DockPanel',a.f[cHc]=4,s4b(a,(m6b(),g6b)),n4b(a,new G2b(JJc),(m4b(),j4b)),n4b(a,new G2b(KJc),k4b),n4b(a,new G2b(LJc),g4b),n4b(a,new G2b(MJc),l4b),n4b(a,new G2b(NJc),j4b),n4b(a,new G2b(OJc),k4b),b=new G2b("Voici un <code>panneau de d\xE9filement<\/code> situ\xE9 au centre d'un <code>panneau d'ancrage<\/code>. Si des contenus relativement volumineux sont ins\xE9r\xE9s au milieu de ce panneau \xE0 d\xE9filement et si sa taille est d\xE9finie, il prend la forme d'une zone dot\xE9e d'une fonction de d\xE9filement \xE0 l'int\xE9rieur de la page, sans l'utilisation d'un IFRAME.<br><br>Voici un texte encore plus obscur qui va surtout servir \xE0 faire d\xE9filer cet \xE9l\xE9ment jusqu'en bas de sa zone visible. Sinon, il vous faudra r\xE9duire ce panneau \xE0 une taille minuscule pour pouvoir afficher ces formidables barres de d\xE9filement!"),c=new z_b(b),c.db.style[fDc]=PJc,c.db.style[dDc]=QJc,n4b(a,c,f4b),o4b(a,'cwDockPanel'),a))};Meb(1047,1003,nAc,v4b);_.Ib=function w4b(a){o4b(this,a)};_._b=function x4b(a){var b;b=hZb(this,a);if(b){a==this.b&&(this.b=null);p4b(this)}return b};_.b=null;var f4b,g4b,h4b,i4b,j4b,k4b,l4b;Meb(1048,1,{},z4b);Meb(1049,1,{97:1},B4b);_.b=null;_.d=null;Meb(1050,1,{98:1},D4b);_.b=0;_.c=null;var U7=npc(SHc,'DockPanel',1047),T7=npc(SHc,'DockPanel$TmpRow',1050),zdb=mpc(ZHc,'DockPanel$TmpRow;',1378,T7),R7=npc(SHc,'DockPanel$DockLayoutConstant',1048),S7=npc(SHc,'DockPanel$LayoutData',1049);MBc(In)(11);