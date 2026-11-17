package com.gempukku.lotro.cards.official.set17;

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

public class Card_17_087_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("master", "17_87");

					put("denizen", "11_115");     // Denizen of Khazad-dum: [orc] Orc, strength 9; +2 only at an underground site
					put("denizen2", "11_115");
					put("halftroll", "14_12");    // Half-troll of Far Harad: [orc] minion who is no Orc; its trigger needs more companions than minions
					put("wargrider", "5_67");     // Warg-rider: [isengard] warg-rider Orc, strength 5; fierce while not exhausted
					put("vicious", "17_92");      // Vicious Warg: [orc] mount, cost 3, +4 strength, +2 vitality, fierce; bearer an [orc] Orc of strength 9 or less
					// Threatening Warg: [orc] mount, +2 strength, fierce; cheated on, so its play trigger is never set off
					put("threatening", "17_91");
					put("gothmog", "18_81");      // Gothmog's Warg, Leader's Mount: [orc] mount, cost 1, fierce
					put("relentless", "17_89");   // Relentless Warg: [orc] mount, for the other player's discard pile
					put("warwarg", "5_64");       // War-warg: an [isengard] mount, +4 strength, for a warg-rider; a skirmish response never set off
					put("gear", "17_90");         // Rider's Gear: an [orc] possession that is no mount (armor), +2 strength
					put("gear2", "17_90");

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: 6, +1 for a Man and +1 for a Hobbit here: 8
					// For the Free Peoples hand: a companion, a possession and an event, none of them playable here.
					put("gimli", "1_13");
					put("axe", "1_9");            // Dwarven Axe: bearer must be a Dwarf
					put("strike", "1_3");         // Axe Strike: a Skirmish event on a Dwarf
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishWargmasterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Warg-master
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Each time a mounted [orc] minion wins a skirmish, you may make the Free Peoples player place a card from his or her hand on top of his or her draw deck.<br><b>Shadow:</b> Exert Orkish Warg-master to play an [orc] mount from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("master");

		assertEquals("Orkish Warg-master", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
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
	public void EachWinByAMountedOrcMinionLetsTheShadowPlayerMakeTheFreePeoplesPlayerPutACardFromHandOnHisDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var strike = scn.GetFreepsCard("strike");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToHand(gimli, axe, strike);

		var master = scn.GetShadowCard("master");
		var denizen = scn.GetShadowCard("denizen");
		var vicious = scn.GetShadowCard("vicious");
		var threatening = scn.GetShadowCard("threatening");

		scn.StartGame();
		assertInHand(gimli, axe, strike);
		scn.MoveMinionsToTable(master, denizen);
		// Mounted [orc] minions of two titles, each made fierce by its Warg.
		scn.AttachCardsTo(master, vicious);
		scn.AttachCardsTo(denizen, threatening);
		assertAttachedTo(vicious, master);
		assertAttachedTo(threatening, denizen);
		scn.SkipToAssignments();
		// The Warg-master, 8 + 4, on Aragorn, 8; the Denizen, 9 + 2, on Gandalf, 8.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, master}, new PhysicalCardImpl[]{gandalf, denizen});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(master));
		scn.ShadowAcceptOptionalTrigger();

		// The Free Peoples player chooses the card, of any kind; he must choose one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, axe, strike));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(axe);
		assertSame(axe, scn.GetFreepsTopOfDeck());
		assertInHand(gimli, strike);
		assertEquals(2, scn.GetFreepsHandCount());

		// The other mounted [orc] minion wins as well.
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(master));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsChooseCard(strike);
		assertSame(strike, scn.GetFreepsTopOfDeck());
		assertInHand(gimli);
		assertEquals(1, scn.GetFreepsHandCount());

		// The fierce round: the Warg-master wins again and is asked again; declined, nothing moves.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, master}, new PhysicalCardImpl[]{gandalf, denizen});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(master));
		scn.ShadowDeclineOptionalTrigger();
		assertInHand(gimli);
		assertEquals(1, scn.GetFreepsHandCount());
		assertSame(strike, scn.GetFreepsTopOfDeck());

		scn.FreepsResolveSkirmish(gandalf);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(master));
		scn.ShadowDeclineOptionalTrigger();
		assertInHand(gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NoOfferWhenAnOrcOrcBearingNoMountOrAMountedIsengardOrcWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToHand(gimli, axe);

		var master = scn.GetShadowCard("master");
		var denizen2 = scn.GetShadowCard("denizen2");
		var gear = scn.GetShadowCard("gear");
		var wargrider = scn.GetShadowCard("wargrider");
		var warwarg = scn.GetShadowCard("warwarg");

		scn.StartGame();
		assertInHand(gimli, axe);
		scn.MoveMinionsToTable(master, denizen2, wargrider);
		scn.AttachCardsTo(denizen2, gear);
		scn.AttachCardsTo(wargrider, warwarg);
		assertAttachedTo(gear, denizen2);
		assertAttachedTo(warwarg, wargrider);
		scn.SkipToAssignments();
		// The Denizen in armor, 9 + 2, on Aragorn, 8; the Warg-rider, 5 + 4, on Gandalf, 8.  The Warg-master
		// is left out.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, denizen2}, new PhysicalCardImpl[]{gandalf, wargrider});
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingSkirmishChoice(scn.P1));

		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInHand(gimli, axe);
		assertEquals(2, scn.GetFreepsHandCount());
		// The Warg-rider is fierce while not exhausted: the fierce round comes, with nothing asked first.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AMountedOrcMinionThatLosesASkirmishMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gimli);

		var master = scn.GetShadowCard("master");
		var vicious = scn.GetShadowCard("vicious");

		scn.StartGame();
		assertInHand(gimli);
		scn.MoveMinionsToTable(master);
		scn.AttachCardsTo(master, vicious);
		assertAttachedTo(vicious, master);
		// Aragorn at 13 beats the Warg-master, 12, without overwhelming him: one wound of his four.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, master);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(master));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(master));
		assertInZone(Zone.SHADOW_CHARACTERS, master);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInHand(gimli);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithTheFreePeoplesHandEmptyAWinMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var master = scn.GetShadowCard("master");
		var vicious = scn.GetShadowCard("vicious");

		scn.StartGame();
		scn.MoveMinionsToTable(master);
		scn.AttachCardsTo(master, vicious);
		assertAttachedTo(vicious, master);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, master);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetFreepsHandCount());
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ShadowAbilityExertsTheWargmasterToPlayAnOrcMountFromTheDiscardPileOnAnOrcOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetShadowCard("master");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var wargrider = scn.GetShadowCard("wargrider");
		var vicious = scn.GetShadowCard("vicious");
		var gothmog = scn.GetShadowCard("gothmog");
		var warwarg = scn.GetShadowCard("warwarg");
		var gear2 = scn.GetShadowCard("gear2");
		var hisRelentless = scn.GetFreepsCard("relentless");

		// Two [orc] mounts, an [orc] possession that is no mount, and an [isengard] mount that the Warg-rider in
		// play could bear.  The other player's [orc] mount lies in his own pile.
		scn.MoveCardsToDiscard(vicious, gothmog, gear2, warwarg, hisRelentless);

		scn.StartGame();
		scn.MoveMinionsToTable(master, denizen, halftroll, wargrider);
		scn.SetTwilight(10);
		assertInDiscard(vicious, gothmog, gear2, warwarg, hisRelentless);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(master));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(master));
		scn.ShadowUseCardAction(master);

		assertEquals(1, scn.GetWoundsOn(master));
		assertTrue(scn.ShadowHasCardChoiceAvailable(vicious, gothmog));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gear2, warwarg, hisRelentless));
		scn.ShadowChooseCardBPFromSelection(vicious);

		// The Vicious Warg's own bearer: an [orc] Orc of strength 9 or less, the Warg-master among them.
		assertTrue(scn.ShadowHasCardChoiceAvailable(master, denizen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(halftroll, wargrider));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(denizen);

		assertAttachedTo(vicious, denizen);
		assertInDiscard(gothmog, gear2, warwarg, hisRelentless);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileTheWargmasterIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetShadowCard("master");
		var vicious = scn.GetShadowCard("vicious");
		scn.MoveCardsToDiscard(vicious);

		scn.StartGame();
		scn.MoveMinionsToTable(master);
		// Vitality 2 with 1 wound: he cannot exert.
		scn.AddWoundsToChar(master, 1);
		assertEquals(1, scn.GetWoundsOn(master));
		assertInDiscard(vicious);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(master));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoOrcMountInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetShadowCard("master");
		var wargrider = scn.GetShadowCard("wargrider");
		var vicious = scn.GetShadowCard("vicious");
		var warwarg = scn.GetShadowCard("warwarg");
		var gear2 = scn.GetShadowCard("gear2");

		// An [orc] possession that is no mount, and a mount of another culture that the Warg-rider could bear.
		scn.MoveCardsToDiscard(gear2, warwarg);

		scn.StartGame();
		scn.MoveMinionsToTable(master, wargrider);
		scn.SetTwilight(10);
		assertInDiscard(gear2, warwarg);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(master));

		scn.MoveCardsToDiscard(vicious);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(master));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithThePoolShortOfTheMountsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetShadowCard("master");
		var vicious = scn.GetShadowCard("vicious");
		scn.MoveCardsToDiscard(vicious);

		scn.StartGame();
		scn.MoveMinionsToTable(master);
		assertInDiscard(vicious);
		scn.FreepsPass();

		// The Vicious Warg costs 3: playing it is part of the price.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(master));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(master));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
