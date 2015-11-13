//
//  MyZip.swift
//  MonStaff
//
//  Created by Aubrey Malabie on 2015/11/09.
//  Copyright © 2015 Aubrey Malabie. All rights reserved.
//

import Foundation
import SwiftyJSON

public class MyZip {
 
    static func zipper(nsURL: NSURL) -> ResponseDTO{
        
        let zip = CkoZip()
        var success: Bool
        var response: ResponseDTO = ResponseDTO()
        
        //  Any string unlocks the component for the 1st 30-days.
        success = zip.UnlockComponent("Anything for 30-day trial")
        if success != true {
            response.statusCode = 9090
            response.message = "Zip is not available"
            print("\(zip.LastErrorText)")
            
            return response
        }
        
        success = zip.OpenZip(nsURL.path)
        if success != true {
            print("\(zip.LastErrorText)")
            
            response.statusCode = 9091
            response.message = "could not open zip file"
            return response
        }
        var count = 0
        let fileManager = NSFileManager.defaultManager()
        let paths0 = NSSearchPathForDirectoriesInDomains(.DocumentDirectory, .UserDomainMask, true)[0] as String
        
        let filePathToWrite0 = "\(paths0)"
        let files:NSDirectoryEnumerator = fileManager.enumeratorAtPath(filePathToWrite0)!
        while let _ = files.nextObject() {
            count++
        }
        print("before unzip, files: \(count)")
        
        //  Returns the number of files and directories unzipped.
        //  Unzips to /my_files, re-creating the directory tree
        //  from the .zip.
        print(" zip filename: \(zip.FileName)")
        
        let paths = NSSearchPathForDirectoriesInDomains(.DocumentDirectory, .UserDomainMask, true)[0] as String
        
        let filePathToWrite = "\(paths)/monitor"
        let x = zip.Unzip(filePathToWrite)
        print("return from unzip \(x)")
        if x.integerValue < 0 {
            print("\(zip.LastErrorText)")
        }
        else {
            print("Success! ******************* we don't know about that")
          
            let files:NSDirectoryEnumerator = fileManager.enumeratorAtPath(filePathToWrite0)!
            var count = 0
            
            var arr:Array<AnyObject> = []
            while let file = files.nextObject() {
                count++
                arr.append(file)
                print(file)
            }
            do {
                let f = arr[count - 1]
                let path = "\(paths)/\(f)"
                let json = try NSString(contentsOfFile: path, encoding: NSUTF8StringEncoding)
                response =  DataParser.parseString(JSON(json))
                
                print("before removeItem, paths \n \(path)\n\(nsURL.path)")
                try fileManager.removeItemAtPath(path)
                try fileManager.removeItemAtPath(nsURL.path!)
            }
            catch {
                print("Problem deleting file")
            }

            print("after unzip, files: \(count)")
        }
        return response
    }
    
    func removeTheFile(path:String, fileManager:NSFileManager) {
        do {
        try fileManager.removeItemAtPath(path)
            
        }
        catch {
            print("Problem deleting ")
        }
        
    }
    
}
