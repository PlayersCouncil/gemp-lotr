package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_025_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saelbeth", "53_25");
					put("arwen", "3_7");        // Arwen, Elven Rider: Elf companion, strength 6; a response only for the Ring-bearer's wounds
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: Elf ally; text only at your site 3
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6; skirmish ability only
					put("phial", "53_24");      // Phial of Galadriel: an [elven] card that is no Elf; resistance +2 only

					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, strength 7, vitality 3; regroup ability only
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2; Damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SaelbethStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Saelbeth, Elven Councilor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 4
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: To play, spot an Elf.
		 * Skirmish: Exert Saelbeth to make a minion strength -1 (or -1 for each Elf you can spot if that minion is an
		 * Uruk-hai).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saelbeth");

		assertEquals("Saelbeth", card.getBlueprint().getTitle());
		assertEquals("Elven Councilor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void CannotBePlayedUntilAnElfCompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saelbeth = scn.GetFreepsCard("saelbeth");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		var phial = scn.GetFreepsCard("phial");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(saelbeth, arwen);
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(frodo, phial);

		scn.StartGame();

		// A Dwarf, a Hobbit and an [elven] artifact: no Elf.
		assertFalse(scn.FreepsPlayAvailable(saelbeth));

		scn.FreepsPlayCard(arwen);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(saelbeth));

		scn.FreepsPlayCard(saelbeth);
		// 2 for Arwen + 2 for Saelbeth
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, saelbeth);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedSpottingAnElfAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saelbeth = scn.GetFreepsCard("saelbeth");
		var silinde = scn.GetFreepsCard("silinde");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(saelbeth);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(silinde);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(saelbeth));
		scn.FreepsPlayCard(saelbeth);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, saelbeth);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityMakesAMinionThatIsNotAnUrukHaiStrengthMinus1ForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saelbeth = scn.GetFreepsCard("saelbeth");
		var arwen = scn.GetFreepsCard("arwen");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(saelbeth);

		var servant = scn.GetShadowCard("servant");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(servant, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, servant);
		// The Uruk Savage is left out of every skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetWoundsOn(saelbeth));
		assertEquals(7, scn.GetStrength(servant));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.FreepsActionAvailable(saelbeth));

		scn.FreepsUseCardAction(saelbeth);
		// Any minion, in this skirmish or in none; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(servant, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, frodo, saelbeth));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(servant);

		assertEquals(1, scn.GetWoundsOn(saelbeth));
		// An Orc, though it is [isengard] and 2 Elves can be spotted: -1 only.
		assertEquals(6, scn.GetStrength(servant));
		assertEquals(5, scn.GetStrength(savage));

		scn.ShadowPass();
		scn.FreepsPass();
		// 6 against 6: the Shadow side wins on the tie, and the Orc survives the skirmish.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(7, scn.GetStrength(servant));
	}

	@Test
	public void OnAnUrukHaiItIsMinus1ForEachElfYouCanSpotForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saelbeth = scn.GetFreepsCard("saelbeth");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var gimli = scn.GetFreepsCard("gimli");
		var phial = scn.GetFreepsCard("phial");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.MoveAlliesToTable(saelbeth);
		scn.AttachCardsTo(frodo, phial);

		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, sentry);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, savage}, new PhysicalCardImpl[]{gimli, sentry});
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(5, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(sentry));

		scn.FreepsUseCardAction(saelbeth);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, sentry));
		scn.FreepsChooseCard(savage);
		// Elves: Saelbeth and Arwen. Gimli, Frodo and the [elven] Phial are no Elves.
		assertEquals(1, scn.GetWoundsOn(saelbeth));
		assertEquals(3, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(sentry));

		scn.ShadowPass();
		// A third Elf, an ally, and Saelbeth fresh again, for a second use.
		scn.MoveAlliesToTable(silinde);
		scn.RemoveWoundsFromChar(saelbeth, 1);
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(saelbeth);
		// The [sauron] Uruk-hai, assigned to Gimli and not in this skirmish.
		scn.FreepsChooseCard(sentry);
		assertEquals(1, scn.GetWoundsOn(saelbeth));
		assertEquals(5, scn.GetStrength(sentry));
		assertEquals(3, scn.GetStrength(savage));

		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(gimli);

		// A skirmish action lasts for its own skirmish only.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(sentry));
	}

	@Test
	public void NotOfferedWhileSaelbethIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saelbeth = scn.GetFreepsCard("saelbeth");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(saelbeth);
		scn.AddWoundsToChar(saelbeth, 1);

		var servant = scn.GetShadowCard("servant");

		scn.StartGame();
		scn.MoveMinionsToTable(servant);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, servant);
		scn.FreepsResolveSkirmish(arwen);

		// Vitality 2 with 1 wound: he cannot exert.
		assertTrue(scn.IsExhausted(saelbeth));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(saelbeth));

		scn.RemoveWoundsFromChar(saelbeth, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(saelbeth));
	}
}
