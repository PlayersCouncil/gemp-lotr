package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_92_031_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "1_30");      // Arwen, Daughter of Elrond: Elf companion, vitality 3
					put("legolas", "1_50");    // Legolas, Greenleaf: Elf companion, vitality 3
					put("haldir", "1_48");     // Haldir, Elf of the Golden Wood: Elf companion, vitality 3
					put("orophin", "1_56");    // Orophin, Lorien Bowman: Elf ally, vitality 3; able to exert, and no companion
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: Man companion, vitality 4
					put("gimli", "1_13");      // Gimli, Son of Gloin: Dwarf companion (not an Elf)
					put("harp", "18_3");       // Thorin's Harp: Free Peoples condition in the support area
					put("summons", "1_242");   // The Dark Lord's Summons: [sauron] condition in the support area
					put("hollowing", "3_54");  // Hollowing of Isengard: [isengard] condition in the support area
					put("bladetip", "1_209");  // Blade Tip: [wraith] condition, here borne by a companion
					put("poison", "8_28");     // Spider Poison: Shadow possession that lies in the support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_31", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "1_30");      // Arwen, Daughter of Elrond: Elf companion, vitality 3
					put("legolas", "1_50");    // Legolas, Greenleaf: Elf companion, vitality 3
					put("summons", "1_242");   // The Dark Lord's Summons: [sauron] condition in the support area
					put("hollowing", "3_54");  // Hollowing of Isengard: [isengard] condition in the support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_31"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_31
		 * Type: MetaSite
		 * Intensity: -1
		 * Game Text: Fellowship: Exert two Elf companions to make a Shadow player discard a Shadow condition.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_31", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-1, card.getBlueprint().getIntensity());
	}

	@Test
	public void ExertTwoElvesToDiscardShadowCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var summons = scn.GetShadowCard("summons");
		var hollowing = scn.GetShadowCard("hollowing");

		scn.MoveCompanionsToTable(arwen, legolas);
		scn.MoveCardsToSupportArea(summons, hollowing);

		scn.StartGame();

		var mod = scn.GetFreepsCard("mod");
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsActionAvailable(mod));
		// Arwen and Legolas are the only two Elf companions, so they are the two who exert
		scn.FreepsUseCardAction(mod);

		// Choose the shadow condition to discard
		assertTrue(scn.ShadowHasCardChoiceAvailable(summons, hollowing));
		scn.ShadowChooseCard(summons);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertInZone(Zone.DISCARD, summons);
		assertInZone(Zone.SUPPORT, hollowing);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnyTwoElfCompanionsExertAndTheShadowPlayerChoosesWhichOfHisConditionsToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var haldir = scn.GetFreepsCard("haldir");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var harp = scn.GetFreepsCard("harp");
		var summons = scn.GetShadowCard("summons");
		var bladetip = scn.GetShadowCard("bladetip");
		var poison = scn.GetShadowCard("poison");

		scn.MoveCompanionsToTable(arwen, legolas, haldir, aragorn);
		scn.MoveAlliesToTable(orophin);
		// Aragorn is of the [elven] culture but no Elf.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.ELVEN));
		scn.MoveCardsToSupportArea(harp);
		scn.MoveCardsToSupportArea(summons, poison);

		scn.StartGame();
		// Blade Tip wounds its bearer at the start of each fellowship phase, so it goes on once this one has begun
		scn.AttachCardsTo(aragorn, bladetip);

		scn.FreepsUseCardAction(mod);
		// Three Elf companions, of whom two exert; the Elf ally and the [elven] Man are not among them
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, legolas, haldir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orophin, aragorn));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(legolas, haldir);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The Shadow player must discard one of his conditions, in his support area or borne by a companion;
		// his possession and the Free Peoples player's condition are not his to choose
		assertTrue(scn.ShadowHasCardChoiceAvailable(summons, bladetip));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(poison, harp));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(bladetip);

		assertInZone(Zone.DISCARD, bladetip);
		assertInZone(Zone.SUPPORT, summons);
		assertInZone(Zone.SUPPORT, poison);
		assertInZone(Zone.SUPPORT, harp);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnlyOneElfMeansNoAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var summons = scn.GetShadowCard("summons");

		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(summons);

		scn.StartGame();

		var mod = scn.GetFreepsCard("mod");
		// Can't exert two elves with only one elf
		assertFalse(scn.FreepsActionAvailable(mod));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NonElfCompanionDoesNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		var summons = scn.GetShadowCard("summons");

		scn.MoveCompanionsToTable(arwen, gimli);
		scn.MoveCardsToSupportArea(summons);

		scn.StartGame();

		var mod = scn.GetFreepsCard("mod");
		// Only 1 elf, Gimli is a dwarf — can't exert two elves
		assertFalse(scn.FreepsActionAvailable(mod));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheSecondElfCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var summons = scn.GetShadowCard("summons");

		scn.MoveCompanionsToTable(arwen, legolas, aragorn);
		scn.MoveAlliesToTable(orophin);
		// Aragorn is of the [elven] culture but no Elf.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.ELVEN));
		// Legolas has vitality 3: with 2 wounds he is exhausted and cannot exert
		scn.AddWoundsToChar(legolas, 2);
		scn.MoveCardsToSupportArea(summons);

		scn.StartGame();

		// Arwen could exert twice, and an Elf ally and an [elven] Man could each exert beside her:
		// none of that is two Elf companions exerting
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertFalse(scn.FreepsActionAvailable(mod));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OpponentCanUse() throws DecisionResultInvalidException, CardNotFoundException {
		// The text has no "your": the modifier the other player holds is the Free Peoples player's to use.
		var scn = GetShadowScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var summons = scn.GetShadowCard("summons");

		scn.MoveCompanionsToTable(arwen, legolas);
		scn.MoveCardsToSupportArea(summons);

		scn.StartGame();

		var mod = scn.GetShadowCard("mod");
		assertTrue(scn.FreepsActionAvailable(mod));

		// Arwen and Legolas are the two who exert, and the Summons is the one condition there is to discard
		scn.FreepsUseCardAction(mod);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertInZone(Zone.DISCARD, summons);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
