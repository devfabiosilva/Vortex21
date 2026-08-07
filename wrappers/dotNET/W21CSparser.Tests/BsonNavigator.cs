using System.Collections;
using MongoDB.Bson;

public static class BsonNavigator
{
    public static object? Navigate(object? obj, params object[] args)
    {
        object? tmp = obj;

        foreach (var arg in args)
        {
            if (tmp == null) 
                return null;

            if (tmp is BsonValue bsonVal && !bsonVal.IsBsonDocument && !bsonVal.IsBsonArray)
            {
                throw new Exception($"Unable to navigate leaf BsonValue: {tmp.GetType().Name}");
            }

            if (tmp is BsonDocument bsonDoc)
            {
                string key = arg.ToString()!;
                if (bsonDoc.TryGetValue(key, out BsonValue? value))
                {
                    tmp = UnwrapBsonValue(value);
                }
                else
                {
                    return null;
                }
            }
            else if (tmp is IDictionary<string, object> dict)
            {
                string key = arg.ToString()!;
                tmp = dict.TryGetValue(key, out var val) ? val : null;
            }
            else if (tmp is BsonArray bsonArray)
            {
                int index = Convert.ToInt32(arg);
                tmp = UnwrapBsonValue(bsonArray[index]);
            }
            else if (tmp is IList list)
            {
                int index = Convert.ToInt32(arg);
                tmp = list[index];
            }
            else
            {
                throw new Exception($"Unable to navigate : {tmp.GetType().Name}");
            }
        }

        return tmp;
    }

    private static object? UnwrapBsonValue(BsonValue value)
    {
        if (value == null || value.IsBsonNull) return null;

        return value.BsonType switch
        {
            BsonType.String => value.AsString,
            BsonType.Double => value.AsDouble,
            BsonType.Int32 => value.AsInt32,
            BsonType.Int64 => value.AsInt64,
            BsonType.Boolean => value.AsBoolean,
            BsonType.DateTime => value.ToUniversalTime(),
            BsonType.Decimal128 => (decimal)value.AsDecimal128,
            BsonType.Document => value.AsBsonDocument, //
            BsonType.Array => value.AsBsonArray,        // 
            _ => BsonTypeMapper.MapToDotNetValue(value) // 
        };
    }
}