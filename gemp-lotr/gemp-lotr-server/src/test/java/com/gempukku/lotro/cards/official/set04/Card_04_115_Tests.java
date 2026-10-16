package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_115_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defend", "4_115");
					put("defend2", "4_115");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man who is an ally, vitality 1; a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: vitality +1 on a [gondor] Man, so the Merchant can exert; a response to a wound on him only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit, no Man; a skirmish ability only
					put("eowyn", "4_270");      // Éowyn, Lady of Rohan: a Man companion who is [rohan]; a response to a wound on an unbound companion only

					put("servant", "3_58");     // Isengard Servant: an [isengard] Orc, vitality 3; a regroup ability only
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2; damage +1 only
					put("saruman", "17_116");   // Saruman, Master of the White Hand: an [uruk-hai] minion who is a Wizard, no Uruk-hai, vitality 4; companions resistance -1, a skirmish ability
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, vitality 3; damage +1 only
					put("brawler", "11_179");   // Brawling Uruk: an [uruk-hai] Uruk-hai, vitality 3; a response to a companion's special ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DefendItandHopeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Defend It and Hope
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert a [gondor] Man to wound a minion once (or twice if that minion is an Uruk-hai).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("defend");

		assertEquals("Defend It and Hope", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAGondorManToWoundAMinionWhoIsNoUrukHaiOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defend = scn.GetFreepsCard("defend");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(aragorn, pippin, eowyn);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToHand(defend);

		var servant = scn.GetShadowCard("servant");
		var guard = scn.GetShadowCard("guard");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(servant, guard, saruman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(defend));

		scn.FreepsPlayCard(defend);
		// The [gondor] Men, a companion and the ally, may pay; the [gondor] Hobbit and the [rohan] Man may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, eowyn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merchant));

		// Every minion is offered, of three cultures and three races; no companion or ally is.
		assertTrue(scn.FreepsHasCardChoiceAvailable(servant, guard, saruman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, pippin, eowyn, merchant));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(servant);

		// The Isengard Servant is an Orc, no Uruk-hai: one wound, of his 3 vitality.
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertInDiscard(defend);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnUrukHaiOfEitherCultureIsWoundedTwiceWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defend = scn.GetFreepsCard("defend");
		var defend2 = scn.GetFreepsCard("defend2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToHand(defend, defend2);

		var savage = scn.GetShadowCard("savage");
		var brawler = scn.GetShadowCard("brawler");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, brawler);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(brawler));

		// The ally pays this time: vitality 1 + 1 with Narsil.
		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(merchant);
		assertEquals(1, scn.GetWoundsOn(merchant));
		scn.FreepsChooseCard(savage);

		// The [isengard] Uruk-hai takes two wounds of his 3 vitality, and the player is asked nothing more.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(brawler));
		assertInDiscard(defend);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();
		// The Merchant is exhausted now, so Aragorn pays, unasked.
		scn.FreepsPlayCard(defend2);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(merchant));
		scn.FreepsChooseCard(brawler);

		// So does the [uruk-hai] Uruk-hai.
		assertEquals(2, scn.GetWoundsOn(brawler));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInDiscard(defend2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnUrukHaiCultureMinionWhoIsNoUrukHaiIsWoundedOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defend = scn.GetFreepsCard("defend");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.FreepsPlayCard(defend);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(saruman);

		// Saruman is of the [uruk-hai] culture and a Wizard: one wound, of his 4 vitality.
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(defend);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyGondorManIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defend = scn.GetFreepsCard("defend");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(aragorn, pippin, eowyn);
		scn.MoveCardsToHand(defend);

		var servant = scn.GetShadowCard("servant");

		scn.StartGame();
		// Vitality 4 with 3 wounds: Aragorn cannot exert.  The Hobbit and the [rohan] Man beside him can.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(servant);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertInHand(defend);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(defend));
	}
}
