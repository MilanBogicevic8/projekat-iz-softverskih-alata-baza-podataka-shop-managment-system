CREATE DATABASE [OnlineProdavnica];
go
USE [OnlineProdavnica];
go
CREATE TABLE [Artikal]
( 
    [IdA]        integer  IDENTITY (1,1)  NOT NULL ,
    [Naziv]      varchar(100)  NULL ,
    [Cena]       decimal(10,3)  NULL ,
    [NaStanju]   integer  NULL ,
    [IdP]        integer  NULL 
);
go
CREATE TABLE [Grad]
( 
    [IdG]        integer  IDENTITY (1,1)  NOT NULL ,
    [Naziv]      varchar(100)  NULL 
);
go
CREATE TABLE [Kupac]
( 
    [IdK]        integer  IDENTITY (1,1)  NOT NULL ,
    [Ime]        varchar(100)  NULL ,
    [Racun]      decimal(10,3)  NULL ,
    [IdG]        integer  NULL 
);
go
CREATE TABLE [Linija]
(   [IdL]        integer  IDENTITY (1,1)  NOT NULL ,
    [Rastojanje] integer  NULL ,
    [IdG1]       integer  NOT NULL ,
    [IdG2]       integer  NOT NULL 
);
go
CREATE TABLE [Popust]
( 
    [IdPop]      integer  IDENTITY (1,1)  NOT NULL ,
    [Vrednost]   integer  NULL ,
    [IdP]        integer  NULL 
);
go
CREATE TABLE [Porudbina]
( 
    [IdPor]              integer  IDENTITY  NOT NULL ,
    [Stanje]             varchar(100)  NULL 
        CONSTRAINT [tip_1554041031] CHECK ([Stanje] IN ('created', 'sent', 'arrived')),
    [DatumKreiranja]     datetime  NULL ,
    [DatumPrijema]    	 datetime  NULL ,
    [IdK]                integer  NULL  ,
    [NajblizaGradProdavnica] integer  NULL ,
    [Rastojanje]            int NULL DEFAULT NULL,
    [UkupnaCena]         decimal(10,3) NULL,
    [ZaSistem]           decimal(10,3) NULL,
    [ZaProdavnice]       decimal(10,3) NULL,
    [ViseOd10000]        int NULL,
    [Od]                 int NULL,
    [Do]                 int NULL,
    [DaniPutovanja]      int NULL,
    [PravaCenaStavki]    decimal(10,3) NULL
    
    
);
go
CREATE TABLE [Prodavnica]
( 
    [IdP]        integer  IDENTITY (1,1)  NOT NULL ,
    [Naziv]      varchar(100)  NULL ,
    [Prihod]     decimal(10,3)  NULL ,
    [IdG]        integer  NULL ,
    [CenaStavke]         decimal(10,3)  NULL

);
go
CREATE TABLE [Stavka]
( 
    [IdS]        integer  IDENTITY (1,1)  NOT NULL ,
    [Kolicina]   integer  NULL ,
    [IdA]        integer  NULL ,
    [IdPor]      integer  NULL ,
    [GradOd]     integer  NULL ,
    [GradDo]     integer  NULL ,
    [Rastojanje] integer  NULL ,
    [CenaStavke] decimal(10,3)  NULL,  -- Dodata kolona CenaStavke
    [PravaCenaStavki] decimal(10,3) DEFAULT 0.0
);
go
CREATE TABLE [Transakcija]
( 
    [IdT]        integer  IDENTITY (1,1)  NOT NULL ,
    [Iznos]      decimal(10,3)  NULL ,
    [IdP]        integer  NULL ,
    [IdPor]      integer  NULL,
    [Datum]      datetime  NULL,
    [Sistemu]    decimal(10,3)  NULL 
);
go
ALTER TABLE [Artikal]
    ADD CONSTRAINT [XPKArtikal] PRIMARY KEY CLUSTERED ([IdA] ASC);
go
ALTER TABLE [Grad]
    ADD CONSTRAINT [XPKGrad] PRIMARY KEY CLUSTERED ([IdG] ASC);
go
ALTER TABLE [Kupac]
    ADD CONSTRAINT [XPKKupac] PRIMARY KEY CLUSTERED ([IdK] ASC);
go
ALTER TABLE [Linija]
    ADD CONSTRAINT [XPKLinija] PRIMARY KEY CLUSTERED ([IdG1] ASC, [IdG2] ASC);
go
ALTER TABLE [Popust]
    ADD CONSTRAINT [XPKPopust] PRIMARY KEY CLUSTERED ([IdPop] ASC);
go
ALTER TABLE [Porudbina]
    ADD CONSTRAINT [XPKPorudbina] PRIMARY KEY CLUSTERED ([IdPor] ASC);
go
ALTER TABLE [Prodavnica]
    ADD CONSTRAINT [XPKProdavnica] PRIMARY KEY CLUSTERED ([IdP] ASC);
go
ALTER TABLE [Stavka]
    ADD CONSTRAINT [XPKStavka] PRIMARY KEY CLUSTERED ([IdS] ASC);
go
ALTER TABLE [Transakcija]
    ADD CONSTRAINT [XPKTransakcija] PRIMARY KEY CLUSTERED ([IdT] ASC);
go
ALTER TABLE [Artikal]
    ADD CONSTRAINT [R_6] FOREIGN KEY ([IdP]) REFERENCES [Prodavnica]([IdP])
    ON DELETE NO ACTION
    ON UPDATE CASCADE;
go
ALTER TABLE [Kupac]
    ADD CONSTRAINT [R_8] FOREIGN KEY ([IdG]) REFERENCES [Grad]([IdG])
    ON DELETE NO ACTION
    ON UPDATE CASCADE;
go
ALTER TABLE [Linija]
    ADD CONSTRAINT [R_4] FOREIGN KEY ([IdG1]) REFERENCES [Grad]([IdG])
    ON DELETE NO ACTION
    ON UPDATE CASCADE;
go
ALTER TABLE [Linija]
    ADD CONSTRAINT [R_5] FOREIGN KEY ([IdG2]) REFERENCES [Grad]([IdG])
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;
go
ALTER TABLE [Popust]
    ADD CONSTRAINT [R_7] FOREIGN KEY ([IdP]) REFERENCES [Prodavnica]([IdP])
    ON DELETE NO ACTION
    ON UPDATE CASCADE;
go
ALTER TABLE [Porudbina]
    ADD CONSTRAINT [R_9] FOREIGN KEY ([IdK]) REFERENCES [Kupac]([IdK])
    ON DELETE NO ACTION
    ON UPDATE CASCADE;
go

ALTER TABLE [Porudbina]
    ADD CONSTRAINT [R_16] FOREIGN KEY ([NajblizaGradProdavnica]) REFERENCES [Grad]([IdG])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION;
go
ALTER TABLE [Prodavnica]
    ADD CONSTRAINT [R_1] FOREIGN KEY ([IdG]) REFERENCES [Grad]([IdG])
    ON DELETE NO ACTION
    ON UPDATE CASCADE;
go
ALTER TABLE [Stavka]
    ADD CONSTRAINT [R_10] FOREIGN KEY ([IdA]) REFERENCES [Artikal]([IdA])
    ON DELETE NO ACTION
    ON UPDATE CASCADE;
go
ALTER TABLE [Stavka]
    ADD CONSTRAINT [R_11] FOREIGN KEY ([IdPor]) REFERENCES [Porudbina]([IdPor])
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;
go
ALTER TABLE [Stavka]
    ADD CONSTRAINT [R_14] FOREIGN KEY ([GradOd]) REFERENCES [Grad]([IdG])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION;
go

ALTER TABLE [Stavka]
    ADD CONSTRAINT [R_15] FOREIGN KEY ([GradDo]) REFERENCES [Grad]([IdG])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION;
go
ALTER TABLE [Transakcija]
    ADD CONSTRAINT [R_12] FOREIGN KEY ([IdP]) REFERENCES [Prodavnica]([IdP])
    ON DELETE NO ACTION
    ON UPDATE CASCADE;
go
ALTER TABLE [Transakcija]
    ADD CONSTRAINT [R_13] FOREIGN KEY ([IdPor]) REFERENCES [Porudbina]([IdPor])
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;
go

