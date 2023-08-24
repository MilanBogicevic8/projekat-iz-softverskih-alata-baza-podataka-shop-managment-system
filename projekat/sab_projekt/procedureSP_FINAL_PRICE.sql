USE [OnlineProdavnica]
GO
/****** Object:  StoredProcedure [dbo].[SP_FINAL_PRICE]    Script Date: 6/19/2023 3:19:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create PROCEDURE [dbo].[SP_FINAL_PRICE]
    @OrderId INT,
	@currentTime datetime,
    @FinalPrice DECIMAL(10, 3) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    -- Izračunaj ukupnu cenu svih stavki u porudžbini
    declare @brojUposlednjih30Veciod10000 int;
	select @brojUposlednjih30Veciod10000=count(*)
	from Porudbina p join Kupac K ON p.IdK=k.IdK join Transakcija t on t.IdPor=p.IdPor
	where datediff(DAY,p.DatumKreiranja,@currentTime)<30 
	and k.IdK=(select IdK from Porudbina where IdPor=@OrderId) 
	and t.Iznos>10000 and p.IdPor!=@OrderId and p.Stanje<>'created'
	

	update Porudbina
	set UkupnaCena=(
		SELECT  COALESCE(SUM(CenaStavke), 0)
		FROM Stavka
		WHERE IdPor = @OrderId
	)*IIF(@brojUposlednjih30Veciod10000>0,0.98,1.00),
	ZaSistem=(
		SELECT  COALESCE(SUM(CenaStavke), 0)
		FROM Stavka
		WHERE IdPor = @OrderId
	)*IIF(@brojUposlednjih30Veciod10000>0,0.03,0.05),
	ViseOd10000=IIF(@brojUposlednjih30Veciod10000>0,1,0),
	ZaProdavnice=(
		SELECT  COALESCE(SUM(CenaStavke), 0)
		FROM Stavka
		WHERE IdPor = @OrderId
	)*0.95,
	PravaCenaStavki=(
		SELECT  COALESCE(SUM(PravaCenaStavki), 0)
		FROM Stavka
		WHERE IdPor = @OrderId
	)
	where Porudbina.IdPor=@OrderId

	SELECT @FinalPrice=UkupnaCena
	from Porudbina
	where Porudbina.IdPor=@OrderId
END;

