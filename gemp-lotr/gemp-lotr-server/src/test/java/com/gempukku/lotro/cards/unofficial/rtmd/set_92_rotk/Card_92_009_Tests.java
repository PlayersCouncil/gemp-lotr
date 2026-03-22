package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_92_009_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Eomer, TMOR, Rohan companion, twilight 3
		put("eomer", "4_267");
		// Theoden, Tall and Proud: when killed, you may play a [Rohan] companion from discard or draw deck
		put("theoden", "8_92");
		// Morgul Slayer, [Sauron] Orc Minion, cost 2, strength 7, vitality 2; has text "Regroup: Exert this minion to wound a companion (except the Ring-bearer)."
		put("slayer", "3_93");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_9", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_9"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_9
		 * Type: MetaSite
		 * Game Text: While your fellowship is at site 6 or higher, companions cannot be played.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_9", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void CompanionCanBePlayed_BeforeSite6() throws DecisionResultInvalidException, CardNotFoundException {
		// At site 5 or lower, companions should be playable normally.

		var scn = GetFreepsScenario();

		var eomer = scn.GetFreepsCard("eomer");

		scn.MoveCardsToHand(eomer);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(eomer));

		scn.SkipToSite(5);

		assertTrue(scn.FreepsPlayAvailable(eomer));
	}

	@Test
	public void CompanionCannotBePlayed_AtSite6OrHigher() throws DecisionResultInvalidException, CardNotFoundException {
		// At site 6+, companions cannot be played.

		var scn = GetFreepsScenario();

		var eomer = scn.GetFreepsCard("eomer");

		scn.MoveCardsToHand(eomer);

		scn.StartGame();
		scn.SkipToSite(6);

		assertFalse(scn.FreepsPlayAvailable(eomer));
	}

	@Test
	public void CompanionCannotBePlayedFromDeck_AtSite6OrHigher() throws DecisionResultInvalidException, CardNotFoundException {
		// Westfold plays a companion from draw deck.  This should also be blocked at site 6+.

		var scn = GetFreepsScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(theoden);

		scn.StartGame();
		scn.SkipToSite(6);

		scn.AddWoundsToChar(theoden, 2);
		scn.MoveCardsToBottomOfDeck(eomer);
		scn.MoveMinionsToTable(slayer);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		//We now kill Theoden, triggering his ability to play a companion from draw deck, which should fail
		scn.ShadowUseCardAction(slayer);
		assertInZone(Zone.DEAD, theoden);
		//Activate Theoden's trigger
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChoose("draw deck");
		scn.FreepsDismissRevealedCards();

		//No actual companion play option was permitted so we are back in the Regroup phase action procedure
		assertInZone(Zone.DECK, eomer);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CompanionCannotBePlayedFromDiscard_AtSite6OrHigher() throws DecisionResultInvalidException, CardNotFoundException {
		// Westfold plays a companion from draw deck.  This should also be blocked at site 6+.

		var scn = GetFreepsScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(theoden);

		scn.StartGame();
		scn.SkipToSite(6);

		scn.AddWoundsToChar(theoden, 2);
		scn.MoveCardsToDiscard(eomer);
		scn.MoveMinionsToTable(slayer);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		//We now kill Theoden, triggering his ability to play a companion from draw deck, which should fail
		scn.ShadowUseCardAction(slayer);
		assertInZone(Zone.DEAD, theoden);
		//Activate Theoden's trigger
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChoose("discard");

		//No actual companion play option was permitted so we are back in the Regroup phase action procedure
		assertInZone(Zone.DISCARD, eomer);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OpponentNotAffected() throws DecisionResultInvalidException, CardNotFoundException {
		// Shadow has the modifier.  Freeps playing a companion at site 6+ should still work.

		var scn = GetShadowScenario();

		var eomer = scn.GetFreepsCard("eomer");

		scn.MoveCardsToHand(eomer);

		scn.StartGame();
		scn.SkipToSite(6);

		assertTrue(scn.FreepsPlayAvailable(eomer));
	}
}
