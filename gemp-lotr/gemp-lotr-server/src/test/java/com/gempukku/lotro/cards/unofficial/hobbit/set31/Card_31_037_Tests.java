package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_037_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Turn 1's Shadow
	// phase and skirmishes are at The Troll Hoard 30_50 (site 2), where every minion here is roaming.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bolg", "31_37");

					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, cost 2; text only at site 9 and in the regroup phase
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, cost 2; an Assignment ability not used
					put("malevolent", "33_47"); // Malevolent Orc: [sauron] Orc, cost 2; an end-of-Shadow-phase take that needs an event in the discard pile
					put("bats", "32_30");       // Dark Bats: [moria] minion, a Creature and no Orc, cost 1; a Shadow ability not used

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, strength 7, vitality 4; a Fellowship ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, vitality 3; a Fellowship ability not used
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] companion; a Maneuver ability not used
					put("fili", "30_10");       // Fili, Brother of Kili: [dwarven] companion; a Response to a wound not used
					put("gloin", "30_11");      // Glóin, Father of Gimli: [dwarven] companion; a Skirmish ability not used
					put("dain", "32_2");        // Dain Ironfoot: a [dwarven] ally; Thorin is strength +1

					put("tauriel", "31_10");    // Tauriel, Staunch Defender: an [elven] archer ally; a Maneuver ability not used
					put("legolas", "1_50");     // Legolas, Greenleaf: an [elven] archer companion (no Hobbit-set [elven] companion is printed)
					put("elrond", "30_21");     // Elrond, Elven Lord: an [elven] ally who is no archer
					put("bard", "30_22");       // Bard, The Bowman: an [esgaroth] archer ally; a Regroup ability not used
					put("kili", "30_12");       // Kili, Brother of Fili: a [dwarven] archer companion; an Archery ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BolgStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Bolg, Servant of Sauron
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>When you play Bolg, you may play an Orc from your discard pile for each [dwarven] companion over 3.<br><b>Maneuver:</b> Spot an [elven] archer to make Bolg fierce until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bolg");

		assertEquals("Bolg", card.getBlueprint().getTitle());
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
	public void WithFourDwarvenCompanionsPlayingBolgPlaysOneOrcFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var fili = scn.GetFreepsCard("fili");
		var dain = scn.GetFreepsCard("dain");
		var freepsMalevolent = scn.GetFreepsCard("malevolent");
		scn.MoveCompanionsToTable(thorin, balin, dwalin, fili);
		scn.MoveAlliesToTable(dain);

		var bolg = scn.GetShadowCard("bolg");
		var veteran = scn.GetShadowCard("veteran");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToHand(bolg);

		scn.StartGame();
		scn.MoveCardsToDiscard(veteran, aggressor, bats, freepsMalevolent);
		assertInHand(bolg);
		assertInZone(Zone.DISCARD, veteran, aggressor, bats, freepsMalevolent);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(bolg);
		assertInZone(Zone.SHADOW_CHARACTERS, bolg);

		// Four [dwarven] companions, one over 3; Bilbo, a companion of another culture, and Dain, a [dwarven] ally,
		// are not counted.
		assertTrue(scn.ShadowDecisionAvailable("Do you want to play an Orc"));
		scn.ShadowChooseYes();

		// Orcs of two cultures; not Dark Bats, a minion who is no Orc, nor the Free Peoples player's Orc in his own
		// discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(veteran, aggressor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats, freepsMalevolent));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aggressor);

		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInZone(Zone.DISCARD, veteran, bats, freepsMalevolent);
		// One Orc, and no second offer.
		assertFalse(scn.ShadowDecisionAvailable("Do you want to play an Orc"));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFiveDwarvenCompanionsPlayingBolgPlaysTwoOrcsFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var fili = scn.GetFreepsCard("fili");
		var gloin = scn.GetFreepsCard("gloin");
		scn.MoveCompanionsToTable(thorin, balin, dwalin, fili, gloin);

		var bolg = scn.GetShadowCard("bolg");
		var veteran = scn.GetShadowCard("veteran");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToHand(bolg);

		scn.StartGame();
		scn.MoveCardsToDiscard(veteran, aggressor, bats);
		assertInHand(bolg);
		assertInZone(Zone.DISCARD, veteran, aggressor, bats);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(bolg);
		assertInZone(Zone.SHADOW_CHARACTERS, bolg);

		assertTrue(scn.ShadowDecisionAvailable("Do you want to play an Orc"));
		scn.ShadowChooseYes();
		scn.ShadowChooseCard(veteran);
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);

		// Two over 3: a second Orc, the one left, played with no question.
		assertTrue(scn.ShadowDecisionAvailable("Do you want to play an Orc"));
		scn.ShadowChooseYes();
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);

		assertInZone(Zone.DISCARD, bats);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningPlaysNoOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var fili = scn.GetFreepsCard("fili");
		scn.MoveCompanionsToTable(thorin, balin, dwalin, fili);

		var bolg = scn.GetShadowCard("bolg");
		var veteran = scn.GetShadowCard("veteran");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToHand(bolg);

		scn.StartGame();
		scn.MoveCardsToDiscard(veteran, aggressor);
		assertInHand(bolg);
		assertInZone(Zone.DISCARD, veteran, aggressor);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(bolg);
		assertTrue(scn.ShadowDecisionAvailable("Do you want to play an Orc"));
		scn.ShadowChooseNo();

		assertInZone(Zone.SHADOW_CHARACTERS, bolg);
		assertInZone(Zone.DISCARD, veteran, aggressor);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreeDwarvenCompanionsNoOrcIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var dain = scn.GetFreepsCard("dain");
		var legolas = scn.GetFreepsCard("legolas");
		// Three [dwarven] companions, with Bilbo and Legolas, companions of other cultures, and Dain, a [dwarven]
		// ally, beside them.
		scn.MoveCompanionsToTable(thorin, balin, dwalin, legolas);
		scn.MoveAlliesToTable(dain);

		var bolg = scn.GetShadowCard("bolg");
		var veteran = scn.GetShadowCard("veteran");
		scn.MoveCardsToHand(bolg);

		scn.StartGame();
		scn.MoveCardsToDiscard(veteran);
		assertInHand(bolg);
		assertInZone(Zone.DISCARD, veteran);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(bolg);

		assertInZone(Zone.SHADOW_CHARACTERS, bolg);
		assertFalse(scn.ShadowDecisionAvailable("Do you want to play an Orc"));
		assertInZone(Zone.DISCARD, veteran);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherOrcPlayedAfterBolgPlaysNoOrcFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var fili = scn.GetFreepsCard("fili");
		scn.MoveCompanionsToTable(thorin, balin, dwalin, fili);

		var bolg = scn.GetShadowCard("bolg");
		var malevolent = scn.GetShadowCard("malevolent");
		var veteran = scn.GetShadowCard("veteran");
		scn.MoveCardsToHand(malevolent);

		scn.StartGame();
		scn.MoveCardsToDiscard(veteran);
		assertInHand(malevolent);
		assertInZone(Zone.DISCARD, veteran);
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bolg);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, bolg);

		// A [sauron] Orc, as Bolg is, played while Bolg is in play.
		scn.ShadowPlayCard(malevolent);
		assertInZone(Zone.SHADOW_CHARACTERS, malevolent);
		assertFalse(scn.ShadowDecisionAvailable("Do you want to play an Orc"));
		assertInZone(Zone.DISCARD, veteran);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOrcInTheDiscardPileNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var fili = scn.GetFreepsCard("fili");
		var freepsMalevolent = scn.GetFreepsCard("malevolent");
		scn.MoveCompanionsToTable(thorin, balin, dwalin, fili);

		var bolg = scn.GetShadowCard("bolg");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToHand(bolg);

		scn.StartGame();
		// A minion who is no Orc in his discard pile, and an Orc in the other player's.
		scn.MoveCardsToDiscard(bats, freepsMalevolent);
		assertInHand(bolg);
		assertInZone(Zone.DISCARD, bats, freepsMalevolent);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(bolg);

		assertInZone(Zone.SHADOW_CHARACTERS, bolg);
		assertFalse(scn.ShadowDecisionAvailable("Do you want to play an Orc"));
		assertInZone(Zone.DISCARD, bats, freepsMalevolent);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverSpotsAnElvenArcherAllyToMakeBolgFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var tauriel = scn.GetFreepsCard("tauriel");
		var elrond = scn.GetFreepsCard("elrond");
		var bard = scn.GetFreepsCard("bard");
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(tauriel, elrond, bard);

		var bolg = scn.GetShadowCard("bolg");

		scn.StartGame();
		scn.MoveMinionsToTable(bolg);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(bolg, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(bolg));
		scn.ShadowUseCardAction(bolg);
		assertTrue(scn.HasKeyword(bolg, Keyword.FIERCE));

		// Fierce already, and still offered (Q-0245).
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(bolg));
		scn.ShadowPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, bolg);
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();
		// Strength 9 against 7: Thorin loses, and takes 2 wounds for Bolg's damage +1.
		assertEquals(2, scn.GetWoundsOn(thorin));

		// The fierce round, and a skirmish in it.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(bolg, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(balin, bolg);
		scn.FreepsResolveSkirmish(balin);
		assertTrue(scn.HasKeyword(bolg, Keyword.FIERCE));
		scn.PassSkirmishActions();

		// The first regroup decision: no longer fierce.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(bolg, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ManeuverIsOfferedOnAnElvenArcherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var kili = scn.GetFreepsCard("kili");
		var elrond = scn.GetFreepsCard("elrond");
		var bard = scn.GetFreepsCard("bard");
		scn.MoveCompanionsToTable(legolas, kili);
		scn.MoveAlliesToTable(elrond, bard);

		var bolg = scn.GetShadowCard("bolg");

		scn.StartGame();
		scn.MoveMinionsToTable(bolg);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(bolg, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(bolg));
		scn.ShadowUseCardAction(bolg);
		assertTrue(scn.HasKeyword(bolg, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverIsNotOfferedWithNoElvenArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kili = scn.GetFreepsCard("kili");
		var elrond = scn.GetFreepsCard("elrond");
		var bard = scn.GetFreepsCard("bard");
		// An [elven] ally who is no archer, an [esgaroth] archer ally and a [dwarven] archer companion.
		scn.MoveCompanionsToTable(kili);
		scn.MoveAlliesToTable(elrond, bard);

		var bolg = scn.GetShadowCard("bolg");

		scn.StartGame();
		scn.MoveMinionsToTable(bolg);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(bolg));
		assertFalse(scn.HasKeyword(bolg, Keyword.FIERCE));
	}
}
