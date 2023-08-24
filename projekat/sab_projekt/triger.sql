USE [OnlineProdavnica]
GO
/****** Object:  Trigger [dbo].[UpdateProdavnicaAndInsertTransakcija]    Script Date: 6/19/2023 3:14:55 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create TRIGGER [dbo].[UpdateProdavnicaAndInsertTransakcija]
ON [dbo].[Porudbina]
AFTER UPDATE
AS
BEGIN
    IF UPDATE([Stanje])
    BEGIN
        DECLARE @IdPor INTEGER;
        DECLARE @UkupnaCena DECIMAL(10, 3);
        
        SELECT @IdPor = IdPor, @UkupnaCena = UkupnaCena
        FROM inserted
        WHERE Stanje = 'arrived';
        
        -- Računanje cene za svaku stavku iz prodavnica
        INSERT INTO [Transakcija] ([Iznos], [IdP], [IdPor], [Sistemu])
        SELECT SUM(s.CenaStavke * 0.95), a.IdP, @IdPor, @UkupnaCena
        FROM [Stavka] s
        INNER JOIN [Artikal] a ON s.IdA = a.IdA
        WHERE s.IdPor = @IdPor
        GROUP BY a.IdP;
        
        -- Povećanje prihoda za svaku prodavnicu
        UPDATE [Prodavnica]
        SET [Prihod] = [Prihod] + (SELECT SUM(s.CenaStavke * 0.95)
                                   FROM [Stavka] s join [Artikal] a ON s.IdA=a.IdA
                                   WHERE s.IdPor = @IdPor
                                     AND a.IdP = [Prodavnica].IdP)
        WHERE [Prodavnica].IdP IN (SELECT DISTINCT a.IdP FROM [Stavka] s join [Artikal] a on s.IdA=a.IdA where s.IdPor = @IdPor);
        
        -- Smanjenje prihoda u tabeli Kupac
        UPDATE [Kupac]
        SET [Racun] = [Racun] - @UkupnaCena
        WHERE [Kupac].IdK = (SELECT IdK FROM [Porudbina] WHERE IdPor = @IdPor);
    END
END;
