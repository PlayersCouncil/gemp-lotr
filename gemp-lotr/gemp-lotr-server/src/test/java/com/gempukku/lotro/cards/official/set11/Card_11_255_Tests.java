package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_255_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, cost 4
					put("radagast", "9_26");    // Radagast, The Brown: a Wizard companion of another title
					// Saruman, The White: a [gandalf] Wizard who is an ally, cost 4
					put("saruman", "32_24");
					// Gandalf, Leader of the Company: another Gandalf, for a Wizard in the discard pile
					put("gandalf2", "11_33");
					// Gandalf, Powerful Guide: a Wizard with a requirement of his own, "To play, spot 2 Hobbits."
					put("guide", "15_29");
					// Ent Avenger: a [gandalf] companion who is no Wizard, cost 4
					put("avenger", "6_27");
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit, vitality 4
					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire] Hobbit, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, vitality 3
					// Bounder: a [shire] Hobbit ally, vitality 2, who can exert
					put("bounder", "1_286");
				}},
				new HashMap<>() {{
					put("site1", "11_239");
					put("site2", "11_255");
					// Crags of Emyn Muil: its text needs region 2
					put("site3", "11_234");
					put("site4", "17_148");
					// Sirannon Ruins: a river and no other text.  The site each player starts at.
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void PinnacleofZirakzigilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Pinnacle of Zirakzigil
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>Mountain</b>. At the start of your fellowship phase, you may exert 3 companions to play a Wizard from your dead pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("Pinnacle of Zirakzigil");

		assertEquals("Pinnacle of Zirakzigil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
	}

	@Test
	public void PinnacleofZirakzigilExerts3CompanionsAtStartOfFellowshipPhaseToPlayWizardFromDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var pinnacle = scn.GetShadowSite("Pinnacle of Zirakzigil");

		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry, pippin);

		// The fellowship comes to the Pinnacle on its first turn, loses both Wizards there, and begins its
		// next turn on it.
		scn.StartGame(scn.GetFreepsSite("Sirannon Ruins"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(pinnacle);
		scn.MoveCardsToDeadPile(gandalf, radagast);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertSame(pinnacle, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertInZone(Zone.DEAD, gandalf);
		assertInZone(Zone.DEAD, radagast);
		assertEquals(0, scn.GetTwilight());

		// There are exactly three companions, so nobody is asked which of them exert.
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.FreepsGetSelectableCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(gandalf);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInZone(Zone.DEAD, radagast);
		// Gandalf's twilight cost of 4 is paid as for any play.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ThreeOfTheCompanionsAbleToExertAreChosenAndAWizardAllyInTheDeadPileMayBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pinnacle = scn.GetShadowSite("Pinnacle of Zirakzigil");

		var gandalf = scn.GetFreepsCard("gandalf");
		var saruman = scn.GetFreepsCard("saruman");
		var gandalf2 = scn.GetFreepsCard("gandalf2");
		var avenger = scn.GetFreepsCard("avenger");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry, pippin, aragorn, gimli);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame(scn.GetFreepsSite("Sirannon Ruins"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(pinnacle);
		// Two Wizards and a [gandalf] companion who is none are dead; another Wizard is in the discard pile.
		scn.MoveCardsToDeadPile(gandalf, saruman, avenger);
		scn.MoveCardsToDiscard(gandalf2);
		// Gimli is exhausted: vitality 3.
		scn.AddWoundsToChar(gimli, 2);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertSame(pinnacle, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsAcceptOptionalTrigger();

		// The Ring-bearer, two Hobbits and a Man may exert; an exhausted Dwarf and an ally may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry, pippin, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, bounder));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(frodo, merry, aragorn);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(bounder));

		// Either Wizard in the dead pile, the companion and the ally; not the Ent beside them, and not the
		// Wizard in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, saruman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger, gandalf2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(saruman);

		assertInZone(Zone.SUPPORT, saruman);
		assertInZone(Zone.DEAD, gandalf, avenger);
		assertInZone(Zone.DISCARD, gandalf2);
		// Saruman's twilight cost of 4
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningExertsNobodyAndLeavesTheWizardInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pinnacle = scn.GetShadowSite("Pinnacle of Zirakzigil");

		var guide = scn.GetFreepsCard("guide");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry, pippin);

		scn.StartGame(scn.GetFreepsSite("Sirannon Ruins"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(pinnacle);
		// Frodo, Merry and Pippin are the 2 Hobbits this Gandalf needs spotted to be played.
		scn.MoveCardsToDeadPile(guide);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertSame(pinnacle, scn.GetCurrentSite());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertInZone(Zone.DEAD, guide);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhenOnlyTwoCompanionsCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pinnacle = scn.GetShadowSite("Pinnacle of Zirakzigil");

		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry, pippin);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame(scn.GetFreepsSite("Sirannon Ruins"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(pinnacle);
		scn.MoveCardsToDeadPile(gandalf);
		// Three companions, and Merry (vitality 4) is exhausted.  The Bounder could exert and is no companion.
		scn.AddWoundsToChar(merry, 3);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertSame(pinnacle, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertInZone(Zone.DEAD, gandalf);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithNoWizardInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pinnacle = scn.GetShadowSite("Pinnacle of Zirakzigil");

		var gandalf = scn.GetFreepsCard("gandalf");
		var gandalf2 = scn.GetFreepsCard("gandalf2");
		var avenger = scn.GetFreepsCard("avenger");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry, pippin, aragorn);

		scn.StartGame(scn.GetFreepsSite("Sirannon Ruins"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(pinnacle);
		// A [gandalf] companion who is no Wizard is dead; one Wizard is in the discard pile and one in hand.
		scn.MoveCardsToDeadPile(avenger);
		scn.MoveCardsToDiscard(gandalf2);
		scn.MoveCardsToHand(gandalf);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		// Four companions could exert.  The play is what the exertions pay for, and there is none to make.
		assertSame(pinnacle, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertInZone(Zone.DEAD, avenger);
		assertInZone(Zone.DISCARD, gandalf2);
		assertInZone(Zone.HAND, gandalf);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheWizardInTheDeadPileCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pinnacle = scn.GetShadowSite("Pinnacle of Zirakzigil");

		var guide = scn.GetFreepsCard("guide");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame(scn.GetFreepsSite("Sirannon Ruins"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(pinnacle);
		scn.MoveCardsToDeadPile(guide);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		// Three companions could exert, and a Wizard is in the dead pile.  He needs 2 Hobbits spotted to
		// be played, and Frodo is the only one.
		assertSame(pinnacle, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertInZone(Zone.DEAD, guide);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfferedToEachPlayerAtTheStartOfHisOwnFellowshipPhaseOnThePinnacleAndNotOnArrivingOrAfterMovingOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruins = scn.GetFreepsSite("Sirannon Ruins");
		var pinnacle = scn.GetShadowSite("Pinnacle of Zirakzigil");
		var crags = scn.GetShadowSite("Crags of Emyn Muil");

		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry, pippin);

		var shadowGandalf = scn.GetShadowCard("gandalf");
		var shadowMerry = scn.GetShadowCard("merry");
		var shadowPippin = scn.GetShadowCard("pippin");
		scn.MoveCompanionsToTable(shadowMerry, shadowPippin);

		scn.StartGame(ruins);
		assertSame(ruins, scn.GetCurrentSite());

		// The first player's Gandalf dies in his first fellowship phase, and he moves to the Pinnacle,
		// which the other player lays: arriving is not the start of a fellowship phase.
		scn.MoveCardsToDeadPile(gandalf);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(pinnacle);
		assertSame(pinnacle, scn.GetCurrentSite());
		assertInZone(Zone.DEAD, gandalf);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second player's first turn begins at the Ruins; his own Gandalf dies in it.
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.ShadowGetSiteNumber());
		scn.MoveCardsToDeadPile(shadowGandalf);

		// The first player's second turn begins on the Pinnacle, the other player's card: the offer is his.
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertSame(pinnacle, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(crags);

		// The second player's second turn begins on the Pinnacle too, and the offer is his: his own three
		// companions exert and his own Gandalf is played.
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.ShadowGetSiteNumber());
		assertSame(pinnacle, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(shadowMerry));
		assertEquals(0, scn.GetWoundsOn(shadowPippin));
		assertInZone(Zone.DEAD, shadowGandalf);

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(shadowMerry));
		assertEquals(1, scn.GetWoundsOn(shadowPippin));
		assertInZone(Zone.FREE_CHARACTERS, shadowGandalf);
		assertInZone(Zone.DEAD, gandalf);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// The first player's third turn begins at the site after it.  His Gandalf is still dead and his
		// three companions could exert; the Pinnacle lies behind him and nothing is offered.
		scn.SkipToOtherPlayersTurn();
		assertEquals(3, scn.FreepsGetSiteNumber());
		assertSame(crags, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertInZone(Zone.DEAD, gandalf);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
