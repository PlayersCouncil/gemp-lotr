package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_020_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sentinels", "2_20");

					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: Elf ally, home 3, vitality 2; copies site 3's text only while you spot your site 3
					put("orophin", "1_56");     // Orophin, Lórien Bowman: Elf ally, home 6, vitality 3; an archery ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf who is a companion, not an ally
					put("grimir", "1_17");      // Grimir, Dwarven Elder: an ally who is not an Elf; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion to bear a Shadow condition

					put("stairs", "1_24");      // Stairs of Khazad-dûm: Free Peoples condition in the support area
					put("armor", "1_8");        // Dwarven Armor: Free Peoples possession on a Dwarf, not a condition

					put("greed", "1_125");      // Greed: Shadow condition in the support area; its ability needs 6 companions
					put("urgency", "19_17");    // Urgency: Shadow condition in the support area; its trigger counts Ents
					put("evil", "1_283");       // You Bring Great Evil: Shadow condition borne by a companion; matters only at a sanctuary
					put("poison", "8_28");      // Spider Poison: Shadow possession borne by a companion, not a condition

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, a skirmish ability only
					put("savage", "1_151");     // Uruk Savage: an Uruk-hai, which is not an Orc; damage +1 only
					// Half-troll of Far Harad: an [orc] minion who is no Orc; its trigger is at the start of the
					// assignment phase
					put("halftroll", "14_12");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SecretSentinelsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Secret Sentinels
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert an Elf ally to discard a condition (or 2 conditions if you spot an Orc).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sentinels");

		assertEquals("Secret Sentinels", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithNoOrcToSpotExertingAnElfAllyDiscardsOneConditionOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinels = scn.GetFreepsCard("sentinels");
		var silinde = scn.GetFreepsCard("silinde");
		var orophin = scn.GetFreepsCard("orophin");
		var legolas = scn.GetFreepsCard("legolas");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");
		var armor = scn.GetFreepsCard("armor");
		var greed = scn.GetShadowCard("greed");
		var evil = scn.GetShadowCard("evil");
		var poison = scn.GetShadowCard("poison");
		var savage = scn.GetShadowCard("savage");
		var halftroll = scn.GetShadowCard("halftroll");

		scn.MoveCardsToHand(sentinels);
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveAlliesToTable(silinde, orophin, grimir);
		scn.MoveCardsToSupportArea(stairs, greed);
		scn.AttachCardsTo(aragorn, evil, poison);
		scn.AttachCardsTo(grimir, armor);

		scn.StartGame();
		// An Uruk-hai and an [orc] Half-troll: minions, and neither of them an Orc.
		scn.MoveMinionsToTable(savage, halftroll);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertTrue(scn.FreepsPlayAvailable(sentinels));

		scn.FreepsPlayCard(sentinels);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(silinde, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, grimir, aragorn));
		scn.FreepsChooseCard(orophin);

		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(stairs, greed, evil));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(poison, armor, savage, halftroll));
		scn.FreepsChooseCard(stairs);

		assertInZone(Zone.DISCARD, stairs);
		assertInZone(Zone.SUPPORT, greed);
		assertAttachedTo(evil, aragorn);
		assertInZone(Zone.DISCARD, sentinels);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithAnOrcToSpotNothingIsAskedAndTwoConditionsMayBeDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinels = scn.GetFreepsCard("sentinels");
		var silinde = scn.GetFreepsCard("silinde");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		var stairs = scn.GetFreepsCard("stairs");
		var armor = scn.GetFreepsCard("armor");
		var greed = scn.GetShadowCard("greed");
		var urgency = scn.GetShadowCard("urgency");
		var evil = scn.GetShadowCard("evil");
		var poison = scn.GetShadowCard("poison");
		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToHand(sentinels);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde, orophin, grimir);
		scn.MoveCardsToSupportArea(stairs, greed, urgency);
		scn.AttachCardsTo(aragorn, evil, poison);
		scn.AttachCardsTo(grimir, armor);

		scn.StartGame();
		// A [sauron] Orc to spot, beside an Uruk-hai.
		scn.MoveMinionsToTable(soldier, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(silinde));
		scn.FreepsPlayCard(sentinels);
		scn.FreepsChooseCard(silinde);
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetWoundsOn(orophin));

		// Nothing is asked about the spot: straight to a choice of 1 or 2 conditions (F-0491: one discard, and with an
		// Orc to spot 0 to 1 more, since "a condition" can be the player's own).
		assertFalse(scn.FreepsDecisionAvailable("spot an Orc"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(stairs, greed, urgency, evil));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(poison, armor, soldier, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(greed, evil);

		assertInZone(Zone.DISCARD, greed, evil);
		assertInZone(Zone.SUPPORT, stairs, urgency);
		assertInZone(Zone.DISCARD, sentinels);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithAnOrcToSpotThePlayerMayDiscardJustOneConditionAndKeepHisOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinels = scn.GetFreepsCard("sentinels");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");
		var greed = scn.GetShadowCard("greed");
		var soldier = scn.GetShadowCard("soldier");

		scn.MoveCardsToHand(sentinels);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToSupportArea(stairs, greed);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(sentinels);
		// The only Elf ally pays with nothing asked.
		assertEquals(1, scn.GetWoundsOn(orophin));

		// Two conditions in play, one of them his own: he may take the Shadow one alone.
		assertFalse(scn.FreepsDecisionAvailable("spot an Orc"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(stairs, greed));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(greed);

		assertInZone(Zone.DISCARD, greed);
		assertInZone(Zone.SUPPORT, stairs);
		assertInZone(Zone.DISCARD, sentinels);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithAMoriaOrcToSpotAndOneConditionInPlayThatConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinels = scn.GetFreepsCard("sentinels");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var greed = scn.GetShadowCard("greed");
		var poison = scn.GetShadowCard("poison");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToHand(sentinels);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToSupportArea(greed);
		scn.AttachCardsTo(aragorn, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(sentinels);
		// The only Elf ally pays with nothing asked.
		assertEquals(1, scn.GetWoundsOn(orophin));

		// Two to discard and one condition there: it goes, the Shadow possession stays, and nothing is asked.
		assertFalse(scn.FreepsDecisionAvailable("spot an Orc"));
		assertInZone(Zone.DISCARD, greed);
		assertAttachedTo(poison, aragorn);
		assertInZone(Zone.DISCARD, sentinels);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotPlayableWhileTheOnlyElfAllyIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinels = scn.GetFreepsCard("sentinels");
		var silinde = scn.GetFreepsCard("silinde");
		var legolas = scn.GetFreepsCard("legolas");
		var grimir = scn.GetFreepsCard("grimir");
		var greed = scn.GetShadowCard("greed");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToHand(sentinels);
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(silinde, grimir);
		scn.MoveCardsToSupportArea(greed);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Vitality 2 with 1 wound: exhausted.  Legolas (an Elf companion) and Grimir (a Dwarf ally) could exert.
		scn.AddWoundsToChar(silinde, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sentinels));

		// The control: the wound taken off, and she can pay.
		scn.RemoveWoundsFromChar(silinde, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(sentinels));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
