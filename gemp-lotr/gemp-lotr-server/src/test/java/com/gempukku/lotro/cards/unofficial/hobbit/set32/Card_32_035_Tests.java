package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_035_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. Turn 1's Shadow
	// phase and skirmishes are at The Troll Hoard 30_50 (site 2), no mountain.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("azog", "32_35");

					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc; text only at site 9 and in the regroup phase
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc; an Assignment ability not used
					put("bats", "32_30");       // Dark Bats: [moria] minion, a Creature; a Shadow ability not used
					put("sack", "31_57");       // Caught In A Sack: a [troll] condition in the support area; no Troll, and Bilbo wins nothing

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, strength 7, vitality 4; a Fellowship ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7, vitality 3, no damage bonus
					put("dain", "32_2");        // Dain Ironfoot: a [dwarven] ally; Thorin is strength +1, a start-of-regroup offer that needs a follower
					put("dori", "30_8");        // Dori: a [dwarven] follower; his bearer may not be overwhelmed unless his strength is tripled

					put("axe", "31_1");         // Dwarven Axe: [dwarven] possession, hand weapon, strength +2; damage +1 only at a mountain
					put("axe2", "31_1");
					put("spear", "32_4");       // Dwarven Spear: [dwarven] possession, hand weapon, strength +2; +1 more against a mounted minion
					put("orcrist", "30_17");    // Orcrist: [dwarven] artifact, hand weapon, on Thorin; damage +1, a trigger on his win
					put("jewel", "31_48");      // The Arkenstone, King's Jewel: a Shadow artifact, strength -2, that Thorin may take from Bilbo
					put("sting", "30_47");      // Sting: [shire] artifact on Bilbo; a trigger on his win
					put("crown", "32_1");       // Crown of Erebor: a [dwarven] artifact in the support area; nothing reached with Thorin in play
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void AzogStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Azog, Servant of Sauron
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>Each time Azog wins a skirmish, you may exert him to discard a possession or artifact borne by a [dwarven] companion.<br><b>Maneuver:</b> Spot 4 minions to make Azog fierce until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("azog");

		assertEquals("Azog", card.getBlueprint().getTitle());
		assertEquals("Servant of Sauron", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningASkirmishExertsAzogToDiscardAPossessionBorneByADwarvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var dori = scn.GetFreepsCard("dori");
		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		var spear = scn.GetFreepsCard("spear");
		var sting = scn.GetFreepsCard("sting");
		var crown = scn.GetFreepsCard("crown");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		scn.AttachCardsTo(balin, axe);
		scn.AttachCardsTo(thorin, spear, dori);
		scn.AttachCardsTo(dain, axe2);
		scn.AttachCardsTo(bilbo, sting);
		assertAttachedTo(axe, balin);
		assertAttachedTo(spear, thorin);
		assertAttachedTo(dori, thorin);
		assertAttachedTo(axe2, dain);
		assertAttachedTo(sting, bilbo);
		assertInZone(Zone.SUPPORT, crown);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, azog);
		scn.FreepsResolveSkirmish(balin);
		// Balin 7 + 2 for the Axe against Azog's 9: a tie, which Azog wins. Balin takes 2 wounds (damage +1).
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(azog));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(azog));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(azog));

		// Possessions borne by [dwarven] companions, the loser's and another's; not the Axe Dain, a [dwarven] ally,
		// bears, nor Sting on Bilbo, a [shire] companion, nor Dori, a follower that is no item, nor the Crown, which
		// nobody bears.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, spear));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axe2, sting, dori, crown));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(axe);

		assertInDiscard(axe);
		assertAttachedTo(spear, thorin);
		assertAttachedTo(dori, thorin);
		assertAttachedTo(axe2, dain);
		assertAttachedTo(sting, bilbo);
		assertInZone(Zone.SUPPORT, crown);
		// The regroup phase, where Dain's start-of-regroup offer is made: Dori is a [dwarven] follower in play.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dain));
	}

	@Test
	public void AnArtifactBorneByADwarvenCompanionCanBeDiscardedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var orcrist = scn.GetFreepsCard("orcrist");
		var jewel = scn.GetShadowCard("jewel");
		scn.MoveCompanionsToTable(thorin, balin);

		scn.StartGame();
		scn.AttachCardsTo(balin, axe);
		// A [dwarven] artifact, and a Shadow artifact Thorin has taken from Bilbo.
		scn.AttachCardsTo(thorin, orcrist, jewel);
		assertAttachedTo(axe, balin);
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(jewel, thorin);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, azog);
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(balin));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(azog));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(azog));

		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, orcrist, jewel));
		scn.ShadowChooseCard(orcrist);

		assertInDiscard(orcrist);
		assertAttachedTo(axe, balin);
		assertAttachedTo(jewel, thorin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.AttachCardsTo(balin, axe);
		assertAttachedTo(axe, balin);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, azog);
		scn.FreepsResolveSkirmish(balin);
		// Balin 7 + 2 for the Axe + 3 given by hand: 12 against Azog's 9. Azog loses and takes one wound.
		scn.ApplyAdHocModifier(new StrengthModifier(null, balin, null, 3));
		assertEquals(12, scn.GetStrength(balin));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(azog));
		assertInZone(Zone.SHADOW_CHARACTERS, azog);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(azog));
		assertAttachedTo(axe, balin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.AttachCardsTo(balin, axe);
		assertAttachedTo(axe, balin);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, azog);
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(azog));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(azog));
		assertAttachedTo(axe, balin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenAzogCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.AttachCardsTo(balin, axe);
		assertAttachedTo(axe, balin);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog);
		// Vitality 3 with 2 wounds: Azog cannot exert.
		scn.AddWoundsToChar(azog, 2);
		assertEquals(2, scn.GetWoundsOn(azog));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, azog);
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();

		// Azog won: Balin took 2 wounds.
		assertEquals(2, scn.GetWoundsOn(balin));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(azog));
		assertEquals(2, scn.GetWoundsOn(azog));
		assertAttachedTo(axe, balin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNothingTheTextNamesTheExertionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var axe2 = scn.GetFreepsCard("axe2");
		var sting = scn.GetFreepsCard("sting");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.AttachCardsTo(dain, axe2);
		scn.AttachCardsTo(bilbo, sting);
		assertAttachedTo(axe2, dain);
		assertAttachedTo(sting, bilbo);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, azog);
		scn.FreepsResolveSkirmish(balin);
		// Balin's 7 against Azog's 9.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(balin));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(azog));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(azog));
		assertAttachedTo(axe2, dain);
		assertAttachedTo(sting, bilbo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ManeuverSpotsFourMinionsToMakeAzogFierceUntilTheRegroupPhaseAndEachWinOffersADiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var veteran = scn.GetShadowCard("veteran");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(thorin, balin);

		scn.StartGame();
		scn.AttachCardsTo(balin, axe);
		scn.AttachCardsTo(thorin, spear);
		assertAttachedTo(axe, balin);
		assertAttachedTo(spear, thorin);
		scn.FreepsPass();
		// Azog and three minions of other cultures and races.
		scn.MoveMinionsToTable(azog, veteran, aggressor, bats);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertFalse(scn.HasKeyword(azog, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(azog));
		scn.ShadowUseCardAction(azog);
		assertTrue(scn.HasKeyword(azog, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(veteran, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(aggressor, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(bats, Keyword.FIERCE));

		// Fierce already, and still offered (Q-0245).
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(azog));
		scn.ShadowPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, azog);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		// Balin 7 + 2 for the Axe against 9: Azog wins the tie.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(balin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(azog));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(axe);
		assertInDiscard(axe);
		assertEquals(1, scn.GetWoundsOn(azog));

		// The fierce round, and a skirmish in it.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(azog, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(thorin, azog);
		scn.FreepsResolveSkirmish(thorin);
		assertTrue(scn.HasKeyword(azog, Keyword.FIERCE));
		// Thorin 7 + 2 for the Spear against 9: Azog wins again, and the offer is made again.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(thorin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(azog));
		scn.ShadowAcceptOptionalTrigger();
		// The Spear, the one left, goes unasked.
		assertInDiscard(spear);
		assertEquals(2, scn.GetWoundsOn(azog));

		// The first regroup decision: no longer fierce.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(azog, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ManeuverIsNotOfferedWithThreeMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var veteran = scn.GetShadowCard("veteran");
		var bats = scn.GetShadowCard("bats");
		var sack = scn.GetShadowCard("sack");

		scn.StartGame();
		scn.FreepsPass();
		// Three minions, Azog among them, and a Shadow card that is no minion.
		scn.MoveMinionsToTable(azog, veteran, bats);
		scn.MoveCardsToSupportArea(sack);
		assertInZone(Zone.SHADOW_CHARACTERS, azog, veteran, bats);
		assertInZone(Zone.SUPPORT, sack);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(azog));
		assertFalse(scn.HasKeyword(azog, Keyword.FIERCE));
	}
}
