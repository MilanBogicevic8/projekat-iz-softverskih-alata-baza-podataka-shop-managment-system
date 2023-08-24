USE [OnlineProdavnica]
GO
/****** Object:  StoredProcedure [dbo].[eraseAll]    Script Date: 6/19/2023 3:18:44 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
/****** Object:  StoredProcedure [dbo].[eraseAll]    Script Date: 6/14/2023 5:51:55 AM ******/
create PROCEDURE [dbo].[eraseAll] 
AS
BEGIN
	DBCC CHECKIDENT ('Artikal', RESEED, 0);
	DBCC CHECKIDENT ('Grad', RESEED, 0);
	DBCC CHECKIDENT ('Kupac', RESEED, 0);
	DBCC CHECKIDENT ('Linija', RESEED, 0);
	DBCC CHECKIDENT ('Popust', RESEED, 0);
	DBCC CHECKIDENT ('Porudbina', RESEED, 0);
	DBCC CHECKIDENT ('Prodavnica', RESEED, 0);
	DBCC CHECKIDENT ('Stavka', RESEED, 0);
	DBCC CHECKIDENT ('Transakcija', RESEED, 0);
	EXEC sp_MSForEachTable 'DISABLE TRIGGER ALL ON ?'
	EXEC sp_MSForEachTable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL'
	EXEC sp_MSForEachTable 'DELETE FROM ?'
	EXEC sp_MSForEachTable 'ALTER TABLE ? CHECK CONSTRAINT ALL'
	EXEC sp_MSForEachTable 'ENABLE TRIGGER ALL ON ?'
END
