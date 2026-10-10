package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_282_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legacy", "51_282");
					put("legacy2", "51_282");
					put("legacy3", "51_282");
					put("legacy4", "51_282");

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("warband", "1_272");    // Orc War Band: [sauron] Orc, strength 10, vitality 2, cost 4, site 6; a skirmish ability used only on purpose
					put("defense", "1_244");    // Desperate Defense of the Ring: another [sauron] condition on a companion; its only text is for a bearer assigned to a skirmish
					put("hunter", "1_256");     // Morgul Hunter: [sauron] Orc, vitality 4; strength +1 for each companion, nothing else
					put("cuguard", "10_81");    // Cirith Ungol Guard: [sauron] Uruk-hai (not an Orc), strength 9, vitality 2, damage +1
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, strength 8; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] companion, strength 7; a skirmish ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] companion, strength 6, Knight and no other text
					put("rohirrim", "17_93");   // Aragorn, Defender of Rohan: [rohan] companion, strength 8; his text needs the Ring-bearer assigned
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] ally, not a companion; a maneuver ability used only on purpose

					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] tale, cost 0, support area; strengthens a Dwarf against a [moria] minion
					put("bowmen", "3_41");      // Gondor Bowmen: [gondor] tale, cost 0, support area; an archery ability used only on purpose
					put("durin", "2_14");       // Till Durin Wakes Again: [dwarven] tale, cost 0, support area; heals a Dwarf moving from underground
					put("lord", "1_21");        // Lord of Moria: [dwarven] condition that is not a tale, support area; each Dwarf is damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWeightofaLegacyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Weight of a Legacy
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Strength: -1
		 * Game Text: To play, exert a [Sauron] Orc. Plays on a [Gondor] companion. Limit 1 per bearer.
		 * While you can spot 2 tales, bearer is strength -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legacy");

		assertEquals("The Weight of a Legacy", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(-1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysForNothingOnAChosenGondorCompanionByExertingAChosenSauronOrcAndMakesHimStrengthMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetShadowCard("legacy");
		var soldier = scn.GetShadowCard("soldier");
		var warband = scn.GetShadowCard("warband");
		var cuguard = scn.GetShadowCard("cuguard");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var knight = scn.GetFreepsCard("knight");
		var rohirrim = scn.GetFreepsCard("rohirrim");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCardsToHand(legacy);
		// Aragorn is unique, so one Aragorn to a table: the [rohan] one here, who is no [gondor] companion.
		scn.MoveCompanionsToTable(boromir, knight, rohirrim);
		scn.MoveAlliesToTable(merchant);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, warband, cuguard, troop);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(8, scn.GetStrength(rohirrim));
		assertTrue(scn.ShadowPlayAvailable(legacy));

		scn.ShadowPlayCard(legacy);
		// Plays on a [gondor] companion: not a companion of another culture, Aragorn though he is, nor a [gondor]
		// ally, nor the Ring-bearer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(boromir, knight));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(rohirrim, merchant, frodo));
		scn.ShadowChooseCard(boromir);

		// To play, exert a [sauron] Orc: not a [sauron] Uruk-hai, nor a [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, warband));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cuguard, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(warband);

		assertEquals(1, scn.GetWoundsOn(warband));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(cuguard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(twilight, scn.GetTwilight());
		assertAttachedTo(legacy, boromir);
		assertNotAttachedTo(legacy, knight);
		// No tales: the printed -1 alone.
		assertEquals(6, scn.GetStrength(boromir));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(8, scn.GetStrength(rohirrim));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilASauronOrcCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetShadowCard("legacy");
		var soldier = scn.GetShadowCard("soldier");
		var warband = scn.GetShadowCard("warband");
		var cuguard = scn.GetShadowCard("cuguard");
		var troop = scn.GetShadowCard("troop");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(legacy, warband);
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, cuguard, troop);
		// Vitality 2 with 1 wound: the one [sauron] Orc in play is exhausted.
		scn.AddWoundsToChar(soldier, 1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(legacy));

		scn.ShadowPlayCard(warband);
		assertEquals(0, scn.GetWoundsOn(warband));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.ShadowPlayAvailable(legacy));

		scn.ShadowPlayCard(legacy);
		assertEquals(1, scn.GetWoundsOn(warband));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(cuguard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertAttachedTo(legacy, boromir);
		assertEquals(6, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACompanionWhoBearsOneIsNotOfferedASecondAndWithEveryGondorCompanionBearingOneNoneCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetShadowCard("legacy");
		var legacy2 = scn.GetShadowCard("legacy2");
		var legacy3 = scn.GetShadowCard("legacy3");
		var legacy4 = scn.GetShadowCard("legacy4");
		var defense = scn.GetShadowCard("defense");
		var hunter = scn.GetShadowCard("hunter");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCardsToHand(legacy2, legacy3, legacy4);
		scn.MoveCompanionsToTable(aragorn, boromir, knight);
		scn.AttachCardsTo(aragorn, legacy);
		// The Knight already bears a different [sauron] condition, which is not a copy of this card.
		scn.AttachCardsTo(knight, defense);

		scn.StartGame();
		scn.MoveMinionsToTable(hunter);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(legacy2));

		scn.ShadowPlayCard(legacy2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(boromir, knight));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		scn.ShadowChooseCard(knight);
		assertAttachedTo(legacy2, knight);
		assertAttachedTo(defense, knight);
		assertEquals(1, scn.GetWoundsOn(hunter));

		// Boromir is now the only [gondor] companion bearing none, and is given the third without a choice.
		scn.ShadowPlayCard(legacy3);
		assertAttachedTo(legacy3, boromir);
		assertEquals(2, scn.GetWoundsOn(hunter));

		// Vitality 4 with 2 wounds: the Orc can still exert, and the card costs nothing; there is no bearer left.
		assertFalse(scn.ShadowPlayAvailable(legacy4));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoTalesInPlayEachBearerIsStrengthMinus2MoreAndAThirdTaleAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetShadowCard("legacy");
		var legacy2 = scn.GetShadowCard("legacy2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var knight = scn.GetFreepsCard("knight");
		var bowmen = scn.GetFreepsCard("bowmen");
		var lord = scn.GetFreepsCard("lord");
		var stairs = scn.GetFreepsCard("stairs");
		var durin = scn.GetFreepsCard("durin");
		scn.MoveCompanionsToTable(aragorn, boromir, knight);
		scn.AttachCardsTo(boromir, legacy);
		scn.AttachCardsTo(aragorn, legacy2);
		scn.MoveCardsToSupportArea(bowmen, lord);
		scn.MoveCardsToHand(stairs, durin);

		scn.StartGame();

		// One tale in play, a condition that is not a tale beside it, and two tales in hand: the printed -1 alone.
		assertEquals(6, scn.GetStrength(boromir));
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(knight));
		// Frodo is strength 3, +1 from The Ruling Ring.
		assertEquals(4, scn.GetStrength(frodo));

		scn.FreepsPlayCard(stairs);
		// A [dwarven] tale beside a [gondor] one: 7 - 1 - 2 and 8 - 1 - 2, each copy on its own bearer.
		assertEquals(4, scn.GetStrength(boromir));
		assertEquals(5, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(4, scn.GetStrength(frodo));

		scn.FreepsPlayCard(durin);
		assertEquals(4, scn.GetStrength(boromir));
		assertEquals(5, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
