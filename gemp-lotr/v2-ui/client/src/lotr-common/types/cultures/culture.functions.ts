import { Mappings } from '../../../common/types/mappings.interface';
import { LotrExpansions, LotrFormats } from '../expansions';
import {
  FreePeopleCultures,
  TwilightCultures,
} from '../filter-types/culture.type';

interface AvailableCultures {
  freePeople: string[];
  twilight: string[];
}

export const getCulturesBySelectedSet = (
  setSelection?: Mappings
): AvailableCultures => {
  // TODO: Fill this out with logic for the various formats etc. We might want to have a single way to "get valid options for format" depending on how it goes
  return {
    freePeople: [
      FreePeopleCultures.Dwarven,
      FreePeopleCultures.Elven,
      FreePeopleCultures.Gandalf,
      FreePeopleCultures.Gollum,
      FreePeopleCultures.Gondor,
      FreePeopleCultures.Men,
      FreePeopleCultures.Rohan,
      FreePeopleCultures.Shire,
    ],
    twilight: [
      TwilightCultures.Dunland,
      TwilightCultures.FallenRealms,
      TwilightCultures.Isengard,
      TwilightCultures.Moria,
      TwilightCultures.Orc,
      TwilightCultures.Raider,
      FreePeopleCultures.Gollum,
    ],
  };
};
