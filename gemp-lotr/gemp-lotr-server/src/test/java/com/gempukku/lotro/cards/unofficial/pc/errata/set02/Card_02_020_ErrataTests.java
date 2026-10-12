package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_020_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sentinels", "52_20");

					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: Elf ally, home 3, vitality 2; copies site 3's text only while you spot your site 3
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, home 6, vitality 3; an archery ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf who is a companion, not an ally
					put("grimir", "1_17");      // Grimir, Dwarven Elder: an ally who is not an Elf; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion to bear a Shadow condition

					put("stairs", "1_24");      // Stairs of Khazad-dum: Free Peoples condition in the support area
					put("armor", "1_8");        // Dwarven Armor: Free Peoples possession, not a condition

					put("greed", "1_125");      // Greed: Shadow condition in the support area; its ability needs 6 companions
					put("voice", "1_142");      // Traitor's Voice: Shadow condition in the support area, to be hindered
					put("urgency", "19_17");    // Urgency: Shadow condition in the support area
					put("evil", "1_283");       // You Bring Great Evil: Shadow condition borne by a companion; matters only at a sanctuary
					put("poison", "8_28");      // Spider Poison: Shadow possession borne by a companion, not a condition

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, a skirmish ability only
					put("savage", "1_151");     // Uruk Savage: an Uruk-hai, which is not an Orc; damage +1 only
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
		 * Game Text: Exert an Elf ally to discard a condition (and hinder another Shadow condition if you can spot an Orc).
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
	public void ExertingAnElfAllyDiscardsAConditionAndWithAnOrcHindersAnotherShadowCondition() throws DecisionResultInvalidException, CardNotFoundException {
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
		var voice = scn.GetShadowCard("voice");
		var urgency = scn.GetShadowCard("urgency");
		var evil = scn.GetShadowCard("evil");
		var poison = scn.GetShadowCard("poison");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToHand(sentinels);
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveAlliesToTable(silinde, orophin, grimir);
		scn.MoveCardsToSupportArea(stairs, greed, voice, urgency);
		scn.AttachCardsTo(aragorn, evil, poison);
		scn.AttachCardsTo(grimir, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.HinderCard(voice);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(orophin));
		assertTrue(scn.FreepsPlayAvailable(sentinels));

		scn.FreepsPlayCard(sentinels);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(silinde, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, grimir, aragorn));
		scn.FreepsChooseCard(orophin);

		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(greed, urgency, evil, stairs));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(voice, poison, armor, troop));
		scn.FreepsChooseCard(greed);

		assertInZone(Zone.DISCARD, greed);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(urgency, evil));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(greed, voice, stairs, poison, armor, troop));
		scn.FreepsChooseCard(evil);

		assertTrue(scn.IsHindered(evil));
		assertAttachedTo(evil, aragorn);
		assertFalse(scn.IsHindered(urgency));
		assertInZone(Zone.SUPPORT, urgency);
		assertInZone(Zone.SUPPORT, stairs);
		assertInZone(Zone.DISCARD, sentinels);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DiscardingAFreePeoplesConditionStillHindersAShadowConditionWhenAnOrcOfAnotherCultureIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinels = scn.GetFreepsCard("sentinels");
		var silinde = scn.GetFreepsCard("silinde");
		var stairs = scn.GetFreepsCard("stairs");
		var greed = scn.GetShadowCard("greed");
		var urgency = scn.GetShadowCard("urgency");
		var soldier = scn.GetShadowCard("soldier");

		scn.MoveCardsToHand(sentinels);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToSupportArea(stairs, greed, urgency);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(sentinels);
		// Silinde is the only Elf ally, so she exerts without asking.
		assertEquals(1, scn.GetWoundsOn(silinde));
		scn.FreepsChooseCard(stairs);

		assertInZone(Zone.DISCARD, stairs);
		assertTrue(scn.FreepsHasCardChoiceAvailable(greed, urgency));
		scn.FreepsChooseCard(urgency);

		assertTrue(scn.IsHindered(urgency));
		assertFalse(scn.IsHindered(greed));
		assertInZone(Zone.SUPPORT, greed);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithoutAnOrcToSpotOnlyTheConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinels = scn.GetFreepsCard("sentinels");
		var orophin = scn.GetFreepsCard("orophin");
		var greed = scn.GetShadowCard("greed");
		var urgency = scn.GetShadowCard("urgency");
		var voice = scn.GetShadowCard("voice");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToHand(sentinels);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToSupportArea(greed, urgency, voice);

		scn.StartGame();
		// An Uruk-hai, which is not an Orc, and an Orc who is hindered and cannot be spotted.
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.HinderCard(troop);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(sentinels);
		scn.FreepsChooseCard(greed);

		assertInZone(Zone.DISCARD, greed);
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertFalse(scn.IsHindered(urgency));
		assertFalse(scn.IsHindered(voice));
		assertInZone(Zone.DISCARD, sentinels);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotPlayableUntilAnElfAllyCanExert() throws DecisionResultInvalidException, CardNotFoundException {
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
		// Silinde is vitality 2: one wound exhausts her.
		scn.AddWoundsToChar(silinde, 1);
		scn.MoveCardsToSupportArea(greed);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsPlayAvailable(sentinels));

		scn.RemoveWoundsFromChar(silinde, 1);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsPlayAvailable(sentinels));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
