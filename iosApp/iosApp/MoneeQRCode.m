#import <CoreImage/CoreImage.h>
#import <Foundation/Foundation.h>

CGImageRef _Nullable moneeCreateQrImage(NSString *payload) {
    NSData *data = [payload dataUsingEncoding:NSUTF8StringEncoding];
    CIFilter *filter = [CIFilter filterWithName:@"CIQRCodeGenerator"];
    [filter setValue:data forKey:@"inputMessage"];
    [filter setValue:@"Q" forKey:@"inputCorrectionLevel"];
    CIImage *output = [filter valueForKey:@"outputImage"];
    if (output == nil) {
        return nil;
    }
    return [[CIContext context] createCGImage:output fromRect:output.extent];
}
