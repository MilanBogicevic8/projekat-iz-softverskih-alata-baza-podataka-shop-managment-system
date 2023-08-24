USE [OnlineProdavnica]
GO
/****** Object:  StoredProcedure [dbo].[CalculateTotalPrice]    Script Date: 6/19/2023 3:17:39 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create PROCEDURE [dbo].[CalculateTotalPrice]
    @OrderId INT,
    @currentTime DATETIME,
    @TotalPrice DECIMAL(10, 3) OUTPUT
AS
BEGIN
    -- Izračunaj broj porudžbina koje zadovoljavaju uslove
    DECLARE @brojUposlednjih30Veciod10000 INT;
    SELECT @brojUposlednjih30Veciod10000 = COUNT(*)
    FROM Porudbina p
    JOIN Kupac k ON p.IdK = k.IdK
    JOIN Transakcija t ON t.IdPor = p.IdPor
    WHERE DATEDIFF(DAY, p.DatumKreiranja, @currentTime) < 30
        AND k.IdK = (SELECT IdK FROM Porudbina WHERE IdPor = @OrderId)
        AND t.Iznos > 10000
        AND p.IdPor != @OrderId
        AND p.Stanje <> 'created';

    -- Izračunaj ukupnu cenu svih stavki u porudžbini
    SELECT @TotalPrice = (
            SELECT COALESCE(SUM(CenaStavke), 0)
            FROM Stavka
            WHERE IdPor = @OrderId
        ) * IIF(@brojUposlednjih30Veciod10000 > 0, 0.98, 1.00);
END
