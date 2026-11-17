package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_028_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Turn 1's phases
	// after the move are at The Troll Hoard 30_50 (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spear", "33_28");

					put("braga", "33_25");      // Braga, Captain of the Guard: [men] Man, strength 9, vitality 3; a Skirmish ability not used
					put("alfrid", "32_25");     // Alfrid, Lickspittle: [men] Man, vitality 3; his texts need an [esgaroth] ally, and none is laid
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; abilities not used
					put("ferny", "33_22");      // Bill Ferny Sr., Servant of Azog: a [gundabad] Man; a Regroup ability not used
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6; an Assignment ability not used
					put("hatred", "30_34");     // Hatred Rekindled: a [gundabad] condition, no minion

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7; a Fellowship ability not used
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] companion, strength 7; a Maneuver ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void SpearoftheGuardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Spear of the Guard
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: 2
		 * Game Text: Bearer must be a [men] Man.
		 * 	Skirmish: Exert bearer to discard the bottom card of your draw deck. If it is a minion, make bearer damage +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spear");

		assertEquals("Spear of the Guard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAMenManOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetShadowCard("spear");
		var braga = scn.GetShadowCard("braga");
		var alfrid = scn.GetShadowCard("alfrid");
		var ted = scn.GetShadowCard("ted");
		var ferny = scn.GetShadowCard("ferny");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToHand(spear);

		scn.StartGame();
		scn.MoveMinionsToTable(braga, alfrid, ted, ferny, aggressor);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(spear));
		scn.ShadowPlayCard(spear);

		// Two [men] Men; not a [men] Hobbit, a [gundabad] Man or an Orc.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(braga, alfrid));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ted, ferny, aggressor));
		scn.ShadowChooseCard(alfrid);

		assertAttachedTo(spear, alfrid);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishExertsBearerToDiscardTheBottomCardAndAMinionThereMakesHimDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		var spear = scn.GetShadowCard("spear");
		var braga = scn.GetShadowCard("braga");
		var alfrid = scn.GetShadowCard("alfrid");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.MoveMinionsToTable(braga, alfrid);
		scn.AttachCardsTo(braga, spear);
		scn.MoveCardsToBottomOfDeck(aggressor);
		assertAttachedTo(spear, braga);
		assertInZone(Zone.DECK, aggressor);
		assertEquals(aggressor, scn.GetShadowBottomOfDeck());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, braga);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(spear));
		assertEquals(aggressor, scn.GetShadowBottomOfDeck());
		var deck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetWoundsOn(braga));
		assertEquals(0, scn.GetKeywordCount(braga, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(alfrid));
		assertEquals(0, scn.GetKeywordCount(alfrid, Keyword.DAMAGE));

		scn.ShadowUseCardAction(spear);

		assertEquals(1, scn.GetWoundsOn(braga));
		assertInZone(Zone.DISCARD, aggressor);
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		// The bottom card was an Orc minion: bearer is damage +1, and the other [men] Man is untouched.
		assertEquals(1, scn.GetKeywordCount(braga, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(alfrid));
		assertEquals(0, scn.GetKeywordCount(alfrid, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ACompanionOrAConditionDiscardedFromTheBottomGivesNoDamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		var spear = scn.GetShadowCard("spear");
		var braga = scn.GetShadowCard("braga");
		// The Shadow player's own copy of Balin: a Free Peoples character, but no minion.
		var shadowBalin = scn.GetShadowCard("balin");
		var hatred = scn.GetShadowCard("hatred");

		scn.StartGame();
		scn.MoveMinionsToTable(braga);
		scn.AttachCardsTo(braga, spear);
		scn.MoveCardsToBottomOfDeck(hatred, shadowBalin);
		assertAttachedTo(spear, braga);
		assertEquals(shadowBalin, scn.GetShadowBottomOfDeck());
		assertEquals(hatred, scn.GetFromBottomOfShadowDeck(2));
		assertInZone(Zone.DECK, shadowBalin, hatred);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, braga);
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();

		assertEquals(shadowBalin, scn.GetShadowBottomOfDeck());
		assertEquals(0, scn.GetWoundsOn(braga));
		scn.ShadowUseCardAction(spear);

		assertEquals(1, scn.GetWoundsOn(braga));
		assertInZone(Zone.DISCARD, shadowBalin);
		assertEquals(0, scn.GetKeywordCount(braga, Keyword.DAMAGE));

		scn.FreepsPass();
		assertEquals(hatred, scn.GetShadowBottomOfDeck());
		scn.ShadowUseCardAction(spear);

		assertEquals(2, scn.GetWoundsOn(braga));
		assertInZone(Zone.DISCARD, hatred);
		assertEquals(0, scn.GetKeywordCount(braga, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishIsNotOfferedWhileBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		var spear = scn.GetShadowCard("spear");
		var braga = scn.GetShadowCard("braga");

		scn.StartGame();
		scn.MoveMinionsToTable(braga);
		scn.AttachCardsTo(braga, spear);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(braga, 2);
		assertEquals(2, scn.GetWoundsOn(braga));
		assertAttachedTo(spear, braga);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, braga);
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();

		assertAttachedTo(spear, braga);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(spear));
	}

	@Test
	public void BearerOutsideTheSkirmishMayUseItAndTheDamageBonusIsGoneInTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		scn.MoveCompanionsToTable(balin, dwalin);

		var spear = scn.GetShadowCard("spear");
		var braga = scn.GetShadowCard("braga");
		var alfrid = scn.GetShadowCard("alfrid");
		var aggressor = scn.GetShadowCard("aggressor");
		var ferny = scn.GetShadowCard("ferny");

		scn.StartGame();
		scn.MoveMinionsToTable(braga, alfrid, aggressor);
		scn.AttachCardsTo(braga, spear);
		// Bill Ferny Sr., left in the Shadow player's deck: a minion that is a Man, not an Orc.
		scn.MoveCardsToBottomOfDeck(ferny);
		assertAttachedTo(spear, braga);
		assertInZone(Zone.DECK, ferny);
		assertEquals(ferny, scn.GetShadowBottomOfDeck());
		scn.SkipToAssignments();
		// Braga is left out of the skirmishes.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, aggressor}, new PhysicalCardImpl[]{dwalin, alfrid});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();

		assertFalse(scn.IsCharAssigned(braga));
		assertTrue(scn.ShadowActionAvailable(spear));
		assertEquals(0, scn.GetKeywordCount(braga, Keyword.DAMAGE));
		scn.ShadowUseCardAction(spear);

		assertEquals(1, scn.GetWoundsOn(braga));
		assertInZone(Zone.DISCARD, ferny);
		assertEquals(1, scn.GetKeywordCount(braga, Keyword.DAMAGE));

		// Gone in the next skirmish: each skirmish is a phase of its own.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(dwalin);
		assertEquals(0, scn.GetKeywordCount(braga, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
