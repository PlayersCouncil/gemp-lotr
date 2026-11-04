package com.gempukku.lotro.cards.official.set15;

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

public class Card_15_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("travellers", "15_66");
					put("travellers2", "15_66");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man who is an ally, vitality 1; a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: vitality +1 on a [gondor] Man, so the Merchant can exert; a response to a wound on him only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit, no Man; a skirmish ability only
					put("eowyn", "4_270");      // Éowyn, Lady of Rohan: a Man companion who is [rohan]; a response to a wound on an unbound companion only

					put("tracking", "15_171");  // Tracking Uruk: an [uruk-hai] Uruk-hai, hunter 1, vitality 3; no other text
					put("breeder", "18_87");    // Orkish Breeder: an [orc] Orc, hunter 1, vitality 3; a Shadow ability used only on purpose
					put("brawler", "11_179");   // Brawling Uruk: an [uruk-hai] Uruk-hai who is no hunter, vitality 3; a response to a companion's special ability only
					put("servant", "3_58");     // Isengard Servant: an [isengard] Orc who is no hunter, vitality 3; a regroup ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoTravellersInThisLandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: No Travellers In This Land
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert a [gondor] Man to wound a minion (or wound that minion twice if it is a hunter).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("travellers");

		assertEquals("No Travellers In This Land", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAGondorManToWoundAMinionWhoIsNoHunterOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var travellers = scn.GetFreepsCard("travellers");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(aragorn, pippin, eowyn);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToHand(travellers);

		var servant = scn.GetShadowCard("servant");
		var brawler = scn.GetShadowCard("brawler");
		var tracking = scn.GetShadowCard("tracking");
		var breeder = scn.GetShadowCard("breeder");

		scn.StartGame();
		scn.MoveMinionsToTable(servant, brawler, tracking, breeder);
		// Vitality 3 with 2 wounds: the Brawling Uruk is exhausted, and still a minion that can be wounded.
		scn.AddWoundsToChar(brawler, 2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(2, scn.GetWoundsOn(brawler));
		assertEquals(0, scn.GetWoundsOn(tracking));
		assertEquals(0, scn.GetWoundsOn(breeder));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(travellers));

		scn.FreepsPlayCard(travellers);
		// The [gondor] Men, a companion and the ally, may pay; the [gondor] Hobbit and the [rohan] Man may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, eowyn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merchant));

		// Every minion is offered, hunters or not, of three cultures and two races, the exhausted one among them;
		// no companion or ally is.
		assertTrue(scn.FreepsHasCardChoiceAvailable(servant, brawler, tracking, breeder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, pippin, eowyn, merchant));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(servant);

		// The Isengard Servant is no hunter: one wound, of his 3 vitality.
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(2, scn.GetWoundsOn(brawler));
		assertEquals(0, scn.GetWoundsOn(tracking));
		assertEquals(0, scn.GetWoundsOn(breeder));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertInDiscard(travellers);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AHunterOfEitherCultureIsWoundedTwiceWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var travellers = scn.GetFreepsCard("travellers");
		var travellers2 = scn.GetFreepsCard("travellers2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToHand(travellers, travellers2);

		var tracking = scn.GetShadowCard("tracking");
		var breeder = scn.GetShadowCard("breeder");

		scn.StartGame();
		scn.MoveMinionsToTable(tracking, breeder);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(tracking));
		assertEquals(0, scn.GetWoundsOn(breeder));

		// The ally pays this time: vitality 1 + 1 with Narsil.
		scn.FreepsPlayCard(travellers);
		scn.FreepsChooseCard(merchant);
		assertEquals(1, scn.GetWoundsOn(merchant));
		scn.FreepsChooseCard(tracking);

		// The [uruk-hai] Uruk-hai hunter takes two wounds of his 3 vitality, and the player is asked nothing more.
		assertEquals(2, scn.GetWoundsOn(tracking));
		assertEquals(0, scn.GetWoundsOn(breeder));
		assertInDiscard(travellers);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();
		// The Merchant is exhausted now, so Aragorn pays, unasked.
		scn.FreepsPlayCard(travellers2);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(merchant));
		scn.FreepsChooseCard(breeder);

		// So does the [orc] Orc hunter.
		assertEquals(2, scn.GetWoundsOn(breeder));
		assertEquals(2, scn.GetWoundsOn(tracking));
		assertInDiscard(travellers2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnUrukHaiWhoIsNoHunterIsWoundedOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var travellers = scn.GetFreepsCard("travellers");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(travellers);

		var brawler = scn.GetShadowCard("brawler");
		var tracking = scn.GetShadowCard("tracking");

		scn.StartGame();
		scn.MoveMinionsToTable(brawler, tracking);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(brawler));
		assertEquals(0, scn.GetWoundsOn(tracking));

		scn.FreepsPlayCard(travellers);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(brawler);

		// The Brawling Uruk is of the Tracking Uruk's culture and race, and no hunter: one wound, of his 3 vitality.
		assertEquals(1, scn.GetWoundsOn(brawler));
		assertEquals(0, scn.GetWoundsOn(tracking));
		assertInDiscard(travellers);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyGondorManIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var travellers = scn.GetFreepsCard("travellers");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(aragorn, pippin, eowyn);
		scn.MoveCardsToHand(travellers);

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
		assertInHand(travellers);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(travellers));
	}
}
