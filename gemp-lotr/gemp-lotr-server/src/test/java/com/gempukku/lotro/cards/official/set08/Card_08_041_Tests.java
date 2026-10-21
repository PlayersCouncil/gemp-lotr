package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("oath", "8_41");
					put("oath2", "8_41");
					put("gknight", "5_35");     // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight is its whole text
					// Shield of Boromir: a [gondor] companion's possession; minion archery total -1, and a text for
					// Boromir alone
					put("shield", "2_38");
					// Cards to hold in hand, one of each type and of two cultures, with no text that reaches a test
					// from there: a [gondor] companion, a [shire] possession (Hobbit Sword), a [shire] event whose
					// phase is the fellowship (Sorry About Everything), a [shire] condition (A Talent for Not Being
					// Seen), and two more [gondor] companions
					put("h1", "5_35");
					put("h2", "1_299");
					put("h3", "1_312");
					put("h4", "1_316");
					put("h5", "5_35");
					put("h6", "5_35");

					// The Witch-king, Lord of Angmar: [wraith] Nazgul, fierce, strength 14, vitality 4
					put("wk", "1_237");
					// The Pale Blade: [wraith] hand weapon possession on The Witch-king; its response needs a Free
					// Peoples condition, of which there is none
					put("blade", "1_221");
					// Black Steed: [wraith] mount possession on a Nazgul; +2 at a plains site
					put("steed", "1_208");
					// Ring of Rancor: [wraith] artifact (a Ring) on a Nazgul; a maneuver ability and a reconcile response
					put("ring", "9_44");
					// In Twilight: [wraith] condition on a [wraith] minion; its win trigger needs the bearer to win
					put("twilight", "19_34");
					// Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("soldier", "1_271");
					put("soldier2", "1_271");
					// Banner of the Eye: [sauron] hand weapon possession on a [sauron] Orc, strength +1; its trigger
					// needs the bearer to win
					put("banner", "6_98");
					put("banner2", "6_98");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OathbreakerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Oathbreaker
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Wraith
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Enduring</b>. To play, add a threat.<br><b>Skirmish:</b> Discard 3 cards from hand to discard a possession borne by a minion skirmishing this companion.
		*/

		var scn = GetScenario();

		var oath = scn.GetFreepsCard("oath");

		assertEquals("Oathbreaker", oath.getBlueprint().getTitle());
		assertNull(oath.getBlueprint().getSubtitle());
		assertFalse(oath.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, oath.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, oath.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, oath.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, oath.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(oath, Keyword.ENDURING));
		assertEquals(2, oath.getBlueprint().getTwilightCost());
		assertEquals(6, oath.getBlueprint().getStrength());
		assertEquals(3, oath.getBlueprint().getVitality());
		assertEquals(6, oath.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, oath.getBlueprint().getSignet());
	}

	@Test
	public void CannotBePlayedUntilAThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oath = scn.GetFreepsCard("oath");
		var gknight = scn.GetFreepsCard("gknight");
		scn.MoveCardsToHand(oath, gknight);

		scn.StartGame();
		// Frodo alone, with 1 threat: no more can be added (Oathbreaker is not in play while his cost is paid).
		scn.AddThreats(1);
		// The choice of fellowship actions was sent before the threat was added.
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.HAND, oath, gknight);
		assertFalse(scn.FreepsPlayAvailable(oath));

		scn.FreepsPlayCard(gknight);
		assertInZone(Zone.FREE_CHARACTERS, gknight);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(oath));

		scn.FreepsPlayCard(oath);
		assertInZone(Zone.FREE_CHARACTERS, oath);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardsThreeCardsToDiscardAPossessionBorneByAMinionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oath = scn.GetFreepsCard("oath");
		var oath2 = scn.GetFreepsCard("oath2");
		var shield = scn.GetFreepsCard("shield");
		var h1 = scn.GetFreepsCard("h1");
		var h2 = scn.GetFreepsCard("h2");
		var h3 = scn.GetFreepsCard("h3");
		var h4 = scn.GetFreepsCard("h4");
		var h5 = scn.GetFreepsCard("h5");
		var h6 = scn.GetFreepsCard("h6");
		scn.MoveCompanionsToTable(oath, oath2);
		scn.AttachCardsTo(oath, shield);
		scn.MoveCardsToHand(h1, h2, h3, h4, h5, h6);

		var wk = scn.GetShadowCard("wk");
		var blade = scn.GetShadowCard("blade");
		var steed = scn.GetShadowCard("steed");
		var ring = scn.GetShadowCard("ring");
		var twilight = scn.GetShadowCard("twilight");
		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var banner = scn.GetShadowCard("banner");
		var banner2 = scn.GetShadowCard("banner2");

		scn.StartGame();
		// Strength enough for the second copy to beat the Soldier with his Banner (8): 6 + 3 = 9.
		scn.ApplyAdHocModifier(new StrengthModifier(null, oath2, null, 3));
		scn.MoveMinionsToTable(wk, soldier, soldier2);
		scn.AttachCardsTo(wk, blade, steed, ring, twilight);
		scn.AttachCardsTo(soldier, banner);
		scn.AttachCardsTo(soldier2, banner2);
		assertAttachedTo(shield, oath);
		assertAttachedTo(blade, wk);
		assertAttachedTo(steed, wk);
		assertAttachedTo(ring, wk);
		assertAttachedTo(twilight, wk);
		assertAttachedTo(banner, soldier);
		assertAttachedTo(banner2, soldier2);
		assertInHand(h1, h2, h3, h4, h5, h6);
		assertEquals(6, scn.GetFreepsHandCount());

		// The phase next door: not offered in the assignment phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(oath));
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{oath, wk}, new PhysicalCardImpl[]{oath2, soldier});
		// A second Orc Soldier joins The Witch-king against the first copy.
		scn.ShadowAssignToMinions(oath, soldier2);

		// The second copy's skirmish, this one waiting for his own: his ability is offered and paid for, and the
		// Banner, borne by a minion skirmishing another companion of his title, is not discarded.
		scn.FreepsResolveSkirmish(oath2);
		assertTrue(scn.FreepsActionAvailable(oath));
		scn.FreepsUseCardAction(oath);
		// A companion, a possession and an event: the cost takes any three cards, of any type or culture.
		scn.FreepsChooseCards(h1, h2, h3);
		assertInDiscard(h1, h2, h3);
		assertInHand(h4, h5, h6);
		assertEquals(3, scn.GetFreepsHandCount());
		assertAttachedTo(banner, soldier);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();
		assertAttachedTo(banner, soldier);

		// His own skirmish, against The Witch-king and the second Soldier.
		scn.FreepsResolveSkirmish(oath);
		assertTrue(scn.FreepsActionAvailable(oath));
		scn.FreepsUseCardAction(oath);
		assertInDiscard(h4, h5, h6);
		assertEquals(0, scn.GetFreepsHandCount());

		// The possessions of the two minions skirmishing him, a Nazgul and an Orc: the Witch-king's hand weapon and
		// mount and the second Soldier's Banner; not the Witch-king's artifact or condition, not the first Soldier's
		// Banner (his skirmish is over), and not Oathbreaker's own Shield.
		assertTrue(scn.FreepsHasCardChoiceAvailable(blade, steed, banner2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ring, twilight, banner, shield));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(blade);

		assertInDiscard(blade);
		assertAttachedTo(banner2, soldier2);
		assertAttachedTo(steed, wk);
		assertAttachedTo(ring, wk);
		assertAttachedTo(twilight, wk);
		assertAttachedTo(banner, soldier);
		assertAttachedTo(shield, oath);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOnlyTwoCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oath = scn.GetFreepsCard("oath");
		var h1 = scn.GetFreepsCard("h1");
		var h2 = scn.GetFreepsCard("h2");
		scn.MoveCompanionsToTable(oath);
		scn.MoveCardsToHand(h1, h2);

		var soldier = scn.GetShadowCard("soldier");
		var banner = scn.GetShadowCard("banner");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.AttachCardsTo(soldier, banner);
		assertAttachedTo(banner, soldier);
		assertInHand(h1, h2);
		assertEquals(2, scn.GetFreepsHandCount());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(oath, soldier);

		scn.FreepsResolveSkirmish(oath);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(oath));
		assertEquals(2, scn.GetFreepsHandCount());
		assertAttachedTo(banner, soldier);
	}
}
