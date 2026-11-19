package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_03_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("veteran", "3_100");
					put("veteran2", "3_100");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: companion, strength 6, vitality 3, damage +1; a skirmish ability used only on purpose
					// Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2; "Maneuver: Exert Thrarin to allow him to
					// participate in archery fire and skirmishes until the regroup phase"
					put("thrarin", "1_27");
					// Sauron's Defenses: [sauron] condition in the support area; its text wants 3 companions in the dead pile
					put("defenses", "1_274");
					// You Bring Great Evil: [sauron] condition borne by a companion; its text works only at a sanctuary
					put("evil", "1_283");
					// Traitor's Voice: [isengard] condition in the support area; its text wants an exhausted Ring-bearer or 5 burdens
					put("voice", "1_142");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcVeteranStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Orc Veteran
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Skirmish:</b> Discard your [sauron] condition to make a character skirmishing this minion strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("veteran");

		assertEquals("Orc Veteran", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityDiscardsYourSauronConditionToMakeTheCharacterSkirmishingHimStrengthMinus1ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var veteran2 = scn.GetShadowCard("veteran2");
		var defenses = scn.GetShadowCard("defenses");
		var evil = scn.GetShadowCard("evil");
		var voice = scn.GetShadowCard("voice");
		// The Free Peoples player's own Sauron's Defenses, left in his support area from a Shadow turn of his: not "your".
		var fpDefenses = scn.GetFreepsCard("defenses");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(defenses, voice, fpDefenses);
		scn.AttachCardsTo(gimli, evil);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, veteran2);
		assertInZone(Zone.SUPPORT, defenses, voice, fpDefenses);
		assertAttachedTo(evil, gimli);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, veteran}, new PhysicalCardImpl[]{gimli, veteran2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.ShadowActionAvailable(veteran));

		scn.ShadowUseCardAction(veteran);
		// His own [sauron] conditions, in the support area and on a companion; not the [isengard] one, nor the
		// other player's, nor a [sauron] card that is no condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(defenses, evil));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(voice, fpDefenses, veteran2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(defenses);

		assertInDiscard(defenses);
		assertAttachedTo(evil, gimli);
		assertInZone(Zone.SUPPORT, voice, fpDefenses);
		assertEquals(7, scn.GetStrength(aragorn));
		// Gimli is assigned to the other copy, and Frodo to nobody.
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		// Aragorn lost at 7 to 10 and took a wound.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(gimli);

		// The skirmish is over and the -1 with it.
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ReachesAnAllySkirmishingHimAndNobodyWhileHeWaitsForHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var veteran2 = scn.GetShadowCard("veteran2");
		var defenses = scn.GetShadowCard("defenses");
		var evil = scn.GetShadowCard("evil");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(defenses);
		scn.AttachCardsTo(frodo, evil);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, veteran2);
		assertInZone(Zone.SUPPORT, defenses);
		assertAttachedTo(evil, frodo);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, veteran2}, new PhysicalCardImpl[]{thrarin, veteran});
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		// In the other copy's skirmish, with Thrarin still waiting for his against this one: nobody is skirmishing
		// this minion, and the cost is paid for nothing.
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(thrarin));
		assertTrue(scn.ShadowActionAvailable(veteran));
		scn.ShadowUseCardAction(veteran);
		assertTrue(scn.ShadowHasCardChoiceAvailable(defenses, evil));
		scn.ShadowChooseCard(defenses);

		assertInDiscard(defenses);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(thrarin));
		assertTrue(scn.IsCharAssignedAgainst(thrarin, veteran));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		// Gimli lost at 6 to 10 and took a wound.
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertEquals(4, scn.GetStrength(thrarin));
		assertTrue(scn.ShadowActionAvailable(veteran));
		// The one [sauron] condition left goes with nothing asked.
		scn.ShadowUseCardAction(veteran);
		assertInDiscard(evil);
		assertEquals(3, scn.GetStrength(thrarin));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithoutASauronConditionOfHisOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var voice = scn.GetShadowCard("voice");
		var fpDefenses = scn.GetFreepsCard("defenses");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(voice, fpDefenses);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran);
		assertInZone(Zone.SUPPORT, voice, fpDefenses);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, veteran);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// An [isengard] condition of his own, and a [sauron] condition that is not his.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(veteran));
		assertEquals(8, scn.GetStrength(aragorn));
	}
}
