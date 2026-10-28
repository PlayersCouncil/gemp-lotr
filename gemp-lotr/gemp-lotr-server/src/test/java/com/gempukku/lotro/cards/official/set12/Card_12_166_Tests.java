package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_166_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadow", "12_166");
					put("shadow2", "12_166");
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: a [wraith] Nazgûl, strength 10; nothing below 2 burdens or 2 wounds on the Ring-bearer
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, strength 12, fierce; nothing below 3 burdens
					put("creeper", "8_75");     // Morgul Creeper: a [wraith] minion who is an Orc and no Nazgûl, strength 8; nothing below 3 burdens
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3, not overwhelmed unless his strength is tripled
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: strength 6, +3 while skirmishing a Nazgûl
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally, strength 8, home site 3; "Fellowship: Exert Elrond to draw a card."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadow", "12_166");
					put("shadow2", "12_166");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: vitality 3; bears no Lingering Shadow
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally; his start-of-turn heal is over before the wounds are laid
					// Master of Healing: [elven] Fellowship or Regroup event, cost 1, "Spot Elrond (or 2 [elven] allies) to
					// heal a companion or ally": a wound removed from the character of the player's choice.
					put("master1", "3_22");
					put("master2", "3_22");
					put("master3", "3_22");
					put("master4", "3_22");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadow", "12_166");
					// Sam, Son of Hamfast: a companion who bears no Lingering Shadow; his fellowship ability is not used
					put("sam", "1_311");
					// The Gaffer, Sam's Father: ally, vitality 2.  "Fellowship: Exert The Gaffer to heal Frodo or Sam."
					put("gaffer", "1_291");
					// Rosie Cotton, Hobbiton Lass: ally, vitality 2.  "Fellowship: Exert Rosie Cotton to heal Sam."
					// (Sam is strength +1 beside her, which no test reads.)
					put("rosie", "1_309");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LingeringShadowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Lingering Shadow
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Skirmish:</b> Transfer this condition from your support area to a character skirmishing a Nazgûl.<br>Each time a wound is removed from bearer, add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shadow");

		assertEquals("Lingering Shadow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void InASkirmishAgainstANazgulACopyInYourSupportAreaIsTransferredToTheCompanionFightingIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		var shadow2 = scn.GetShadowCard("shadow2");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(aragorn, boromir, arwen);

		scn.StartGame();
		scn.MoveCardsToSupportArea(shadow, shadow2);
		assertInZone(Zone.SUPPORT, shadow, shadow2);
		scn.MoveMinionsToTable(nelya, khamul, creeper);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nelya}, new PhysicalCardImpl[]{boromir, creeper},
				new PhysicalCardImpl[]{arwen, khamul});

		// First, Boromir against the Morgul Creeper, a [wraith] minion who is no Nazgûl.  Aragorn and Arwen are
		// assigned to Nazgûl, but their skirmishes have not begun: nobody is skirmishing a Nazgûl.
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(shadow));
		assertFalse(scn.ShadowActionAvailable(shadow2));
		scn.ShadowPass();

		// 8 against 7: Boromir loses.
		assertEquals(1, scn.GetWoundsOn(boromir));

		// Now Aragorn against Nelya: each copy offers itself.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(shadow));
		assertTrue(scn.ShadowActionAvailable(shadow2));

		scn.ShadowUseCardAction(shadow);

		// Aragorn is the one character skirmishing a Nazgûl, so nothing is asked; Arwen, still waiting for Khamûl,
		// is not given it.
		assertAttachedTo(shadow, aragorn);
		assertNotAttachedTo(shadow, arwen);
		assertInZone(Zone.SUPPORT, shadow2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The copy on Aragorn is no longer in your support area and offers nothing; the other still does.
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(shadow));
		assertTrue(scn.ShadowActionAvailable(shadow2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllySkirmishingANazgulIsGivenIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		var khamul = scn.GetShadowCard("khamul");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(shadow);
		assertInZone(Zone.SUPPORT, shadow);
		scn.FreepsPass();
		// Site 3: Elrond's home, where he may be assigned.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(khamul);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, khamul);
		scn.FreepsResolveSkirmish(elrond);
		scn.FreepsPass();
		assertInZone(Zone.SUPPORT, shadow);
		assertTrue(scn.ShadowActionAvailable(shadow));

		scn.ShadowUseCardAction(shadow);

		assertAttachedTo(shadow, elrond);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void EachWoundRemovedFromABearerOfEitherKindAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBearerScenario();

		var shadow = scn.GetShadowCard("shadow");
		var shadow2 = scn.GetShadowCard("shadow2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var elrond = scn.GetFreepsCard("elrond");
		var master1 = scn.GetFreepsCard("master1");
		var master2 = scn.GetFreepsCard("master2");
		var master3 = scn.GetFreepsCard("master3");
		var master4 = scn.GetFreepsCard("master4");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(master1, master2, master3, master4);
		assertInHand(master1, master2, master3, master4);

		scn.StartGame();
		// One copy on a companion, one on an ally, as two transfers in skirmishes against Nazgûl would leave them.
		scn.AttachCardsTo(aragorn, shadow);
		scn.AttachCardsTo(elrond, shadow2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(elrond, 1);
		scn.FreepsRefreshDecision();
		assertAttachedTo(shadow, aragorn);
		assertAttachedTo(shadow2, elrond);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetBurdens());

		// Boromir, who bears neither copy, is healed: no burden.
		scn.FreepsPlayCard(master1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, elrond));
		scn.FreepsChooseCard(boromir);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetBurdens());

		// Aragorn, a companion bearer: one burden, from his copy alone.
		scn.FreepsPlayCard(master2);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetBurdens());

		// Elrond, an ally bearer: one more.
		scn.FreepsPlayCard(master3);
		scn.FreepsChooseCard(elrond);
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetBurdens());

		// Aragorn's second wound removed: another burden.
		scn.FreepsPlayCard(master4);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AWoundRemovedFromBearerAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHealScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var gaffer = scn.GetFreepsCard("gaffer");
		var rosie = scn.GetFreepsCard("rosie");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(gaffer, rosie);

		// The Shadow player's condition, on Frodo as it would be after his skirmish with a Nazgûl.
		var shadow = scn.GetShadowCard("shadow");
		scn.AttachCardsTo(frodo, shadow);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(sam, 1);

		assertAttachedTo(shadow, frodo);
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));

		// The Gaffer heals Frodo or Sam, and both are wounded: Frodo, the bearer, is chosen.
		scn.FreepsUseCardAction(gaffer);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(frodo);

		// One wound is gone from the bearer, and one burden has come for it.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(sam));

		// A wound removed from Sam, who bears no Lingering Shadow, adds none.
		scn.FreepsUseCardAction(rosie);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
