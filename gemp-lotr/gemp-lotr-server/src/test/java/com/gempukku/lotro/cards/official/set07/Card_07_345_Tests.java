package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_345_Tests
{

	// The King path: Pelennor Flat is its site 5.  Nothing on the way there asks anything of a fellowship of
	// Frodo alone (Edoras Hall, King's Tent, The Dimholt, Osgiliath Fallen).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// A hand of both sides and several card types: a player's hand holds his Shadow cards too.
					put("guard", "1_7");        // Dwarf Guard: a Free Peoples companion
					put("armor", "1_8");        // Dwarven Armor: a Free Peoples possession
					put("sleep", "1_84");       // Sleep, Caradhras: a Free Peoples event
					put("runner", "1_178");     // Goblin Runner: a Shadow minion
					put("fear", "1_278");       // Strength Born of Fear: a Shadow condition

					put("savage", "1_151");     // Uruk Savage: a minion on the table, so that there is a maneuver phase; "Damage +1." is its whole text
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PelennorFlatStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Pelennor Flat
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 5
		 * Shadow Number: 6
		 * Direction: Left
		 * Game Text: <b>Plains</b>. At the start of the maneuver phase, the Free Peoples player must discard his or her hand or add 2 burdens.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(5);

		assertEquals("Pelennor Flat", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(5, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseTheFreePeoplesPlayerMayDiscardHisWholeHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var sleep = scn.GetFreepsCard("sleep");
		var runner = scn.GetFreepsCard("runner");
		var fear = scn.GetFreepsCard("fear");
		var shadowRunner = scn.GetShadowCard("runner");
		var shadowFear = scn.GetShadowCard("fear");
		var savage = scn.GetShadowCard("savage");
		var flat = scn.GetShadowSite(5);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetFreepsHand(guard, armor, sleep, runner, fear);
		scn.SetShadowHand(shadowRunner, shadowFear);
		scn.FreepsPass();

		// The Shadow player laid the site, and it is his copy that lies there.
		assertSame(flat, scn.GetCurrentSite());
		scn.MoveMinionsToTable(savage);

		// Nothing happens in the Shadow phase.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		assertInHand(guard, armor, sleep, runner, fear);
		assertInHand(shadowRunner, shadowFear);

		scn.ShadowPass();

		// It is the Free Peoples player who is asked, whoever's site this is.
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertInHand(guard, armor, sleep, runner, fear);

		scn.FreepsChooseOption("Discard");

		// His whole hand, the Shadow cards in it as well, and no burden.
		assertInDiscard(guard, armor, sleep, runner, fear);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		assertInHand(shadowRunner, shadowFear);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The start of the next phase asks nothing more.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(0, scn.GetBurdens());
		assertInHand(shadowRunner, shadowFear);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseTheFreePeoplesPlayerMayAddTwoBurdensAndKeepHisHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var sleep = scn.GetFreepsCard("sleep");
		var runner = scn.GetFreepsCard("runner");
		var fear = scn.GetFreepsCard("fear");
		var shadowRunner = scn.GetShadowCard("runner");
		var shadowFear = scn.GetShadowCard("fear");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.SkipToSite(4);
		scn.SetFreepsHand(guard, armor, sleep, runner, fear);
		scn.SetShadowHand(shadowRunner, shadowFear);
		scn.FreepsPass();

		assertEquals("Pelennor Flat", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(savage);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());

		scn.ShadowPass();

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetBurdens());
		scn.FreepsChooseOption("burden");

		// Two burdens, on his own Ring-bearer, and every card still in hand.
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		assertInHand(guard, armor, sleep, runner, fear);
		assertInHand(shadowRunner, shadowFear);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The start of the next phase adds nothing more.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(3, scn.GetBurdens());
		assertInHand(guard, armor, sleep, runner, fear);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	@Ignore("Q-0255: with no card in hand the Free Peoples player is still asked, and may take 'Discard your hand' for nothing and add no burden")
	public void WithNoCardInHandTheFreePeoplesPlayerIsNotAskedAndTwoBurdensAreAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowRunner = scn.GetShadowCard("runner");
		var shadowFear = scn.GetShadowCard("fear");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetFreepsHand();
		scn.SetShadowHand(shadowRunner, shadowFear);
		scn.FreepsPass();

		assertEquals("Pelennor Flat", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(savage);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());

		scn.ShadowPass();

		// A hand of no cards cannot be discarded, so there is nothing to choose between: the burdens are added.
		// The Shadow player's hand is not the one the site speaks of.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		assertInHand(shadowRunner, shadowFear);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
