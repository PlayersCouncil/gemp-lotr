package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_101_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("observer1", "15_101");
					put("observer2", "15_101");  // a second copy: not unique
					put("retribution", "12_101"); // Retribution: [orc] support-area condition; a win trigger and a Shadow ability used only on purpose
					put("blow", "15_115");        // Pummeling Blow: [orc] condition borne by the fellowship's current site; Trolls +1 only
					put("goaded", "12_67");       // Goaded to War: a [men] condition; adds (1) only as the fellowship moves to a battleground or plains
					put("spear", "12_96");        // Orc Spear: an [orc] possession (hand weapon) for an [orc] minion; its (2) only when played
					put("denizen", "11_115");     // Denizen of Khazad-dum: [orc] Orc, strength 9; +2 only at an underground site
					put("corps", "11_73");        // Corps of Harad: [men] Man minion, strength 10; fierce only at a battleground (none on this path)
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("gimli", "1_13");         // Gimli, Son of Gloin: [dwarven] Dwarf companion; a skirmish ability used only on purpose
					put("sword", "1_112");        // Ranger's Sword: strength +2, Aragorn only; damage +1
					put("glorfindel", "9_16");    // Glorfindel, Revealed in Wrath: [elven] Elf companion, strength 9; abilities used only on purpose
					put("lindenroot", "5_19");    // Lindenroot, Elder Shepherd: [gandalf] Ent companion, strength 10; unhasty
					put("tom", "9_52");           // Tom Bombadil, The Master: an ally of strength 14; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackLandObserverStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Black Land Observer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: When you play this minion, you may spot an [orc] condition to add a threat.<br>While you can spot a companion of strength 10 or more, this minion is strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("observer1");

		assertEquals("Black Land Observer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedItMaySpotAnOrcSupportAreaConditionToAddAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Three companions: room for three threats, so one added cannot be told from more by a limit.
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var observer1 = scn.GetShadowCard("observer1");
		var retribution = scn.GetShadowCard("retribution");
		scn.MoveCardsToHand(observer1);
		scn.MoveCardsToSupportArea(retribution);

		scn.StartGame();
		assertInZone(Zone.HAND, observer1);
		assertInZone(Zone.SUPPORT, retribution);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		scn.ShadowPlayCard(observer1);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(observer1));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, observer1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcConditionBorneByTheSiteWillDoAndDecliningAddsNoThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var observer1 = scn.GetShadowCard("observer1");
		var blow = scn.GetShadowCard("blow");
		scn.MoveCardsToHand(observer1);

		scn.StartGame();
		assertInZone(Zone.HAND, observer1);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Pummeling Blow on the site the fellowship has just moved to, and no other [orc] condition.
		scn.AttachCardsTo(scn.GetCurrentSite(), blow);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.ATTACHED, blow);

		assertEquals(0, scn.GetThreats());
		scn.ShadowPlayCard(observer1);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(observer1));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, observer1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAMenConditionAndAnOrcPossessionButNoOrcConditionNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var observer1 = scn.GetShadowCard("observer1");
		var goaded = scn.GetShadowCard("goaded");
		var denizen = scn.GetShadowCard("denizen");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(observer1);

		scn.StartGame();
		assertInZone(Zone.HAND, observer1);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// A [men] condition, an [orc] possession on an [orc] Orc, and the Observer itself, an [orc] minion.
		scn.MoveCardsToSupportArea(goaded);
		scn.MoveMinionsToTable(denizen);
		scn.AttachCardsTo(denizen, spear);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, goaded);
		assertInZone(Zone.ATTACHED, spear);

		assertEquals(0, scn.GetThreats());
		scn.ShadowPlayCard(observer1);

		assertInZone(Zone.SHADOW_CHARACTERS, observer1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreatsAtTheLimitNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var observer1 = scn.GetShadowCard("observer1");
		var retribution = scn.GetShadowCard("retribution");
		scn.MoveCardsToHand(observer1);
		scn.MoveCardsToSupportArea(retribution);

		scn.StartGame();
		assertInZone(Zone.HAND, observer1);
		assertInZone(Zone.SUPPORT, retribution);
		// Three threats on three companions: no threat can be added.
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		assertEquals(3, scn.GetThreatLimit());
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(observer1);

		assertInZone(Zone.SHADOW_CHARACTERS, observer1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMinionPlayedAfterItSetsNothingOffAndASecondCopyOffersOnlyItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var observer1 = scn.GetShadowCard("observer1");
		var observer2 = scn.GetShadowCard("observer2");
		var denizen = scn.GetShadowCard("denizen");
		var retribution = scn.GetShadowCard("retribution");
		scn.MoveCardsToHand(observer2, denizen);
		scn.MoveCardsToSupportArea(retribution);

		scn.StartGame();
		assertInZone(Zone.HAND, observer2);
		assertInZone(Zone.HAND, denizen);
		assertInZone(Zone.SUPPORT, retribution);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.MoveMinionsToTable(observer1);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, observer1);

		// Another [orc] Orc played while an Observer is in play: no offer.
		scn.ShadowPlayCard(denizen);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second copy played: its own offer, and none from the copy already in play.
		scn.ShadowPlayCard(observer2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(observer2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(observer1));
		scn.ShadowChooseOptionalTrigger(observer2);

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileACompanionOfStrength10OrMoreCanBeSpottedThisMinionIsStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		var tom = scn.GetFreepsCard("tom");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn, glorfindel);
		scn.MoveAlliesToTable(tom);
		scn.MoveCardsToHand(sword);

		var observer1 = scn.GetShadowCard("observer1");
		var observer2 = scn.GetShadowCard("observer2");
		var corps = scn.GetShadowCard("corps");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveMinionsToTable(observer1, observer2, corps, denizen);
		scn.FreepsRefreshDecision();

		// Glorfindel is one short, at 9; Tom Bombadil (14) is an ally and the Corps of Harad (10) a minion.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(glorfindel));
		assertEquals(14, scn.GetStrength(tom));
		assertEquals(10, scn.GetStrength(corps));
		assertEquals(9, scn.GetStrength(denizen));
		assertEquals(8, scn.GetStrength(observer1));
		assertEquals(8, scn.GetStrength(observer2));
		assertInZone(Zone.HAND, sword);

		scn.FreepsPlayCard(sword);

		// Aragorn, 8 + 2, is exactly 10: each Observer is 8 + 3 by its own text, the other copy adding nothing to
		// it; the Denizen, another [orc] Orc, is not touched.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(11, scn.GetStrength(observer1));
		assertEquals(11, scn.GetStrength(observer2));
		assertEquals(9, scn.GetStrength(denizen));
		assertEquals(10, scn.GetStrength(corps));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionOfAnotherCultureAndRacePrintedAtStrength10WillDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lindenroot = scn.GetFreepsCard("lindenroot");
		scn.MoveCompanionsToTable(lindenroot);

		var observer1 = scn.GetShadowCard("observer1");

		scn.StartGame();
		scn.MoveMinionsToTable(observer1);

		assertEquals(10, scn.GetStrength(lindenroot));
		// 8 + 3
		assertEquals(11, scn.GetStrength(observer1));
	}
}
