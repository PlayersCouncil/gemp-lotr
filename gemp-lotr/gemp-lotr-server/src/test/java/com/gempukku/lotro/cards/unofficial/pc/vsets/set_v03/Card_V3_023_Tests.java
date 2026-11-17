package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_023_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gate", "103_23");
					put("knight1", "5_35");        // Gondorian Knight: [gondor] Man, knight, vitality 3, cost 2, no other text
					put("knight2", "5_35");
					put("knight3", "5_35");
					put("pippin", "103_31");       // Pippin, Trollslayer: [gondor] Hobbit, knight, vitality 4; a skirmish ability only
					put("oath", "8_41");           // Oathbreaker: [gondor] Wraith companion, no knight, vitality 3
					put("rohanAragorn", "17_93");  // Aragorn, Defender of Rohan: a [rohan] companion; his text needs the Ring-bearer assigned
					put("merchant", "7_97");       // Gondorian Merchant: the one [gondor] ally, vitality 1; a maneuver ability only
					put("stronghold", "102_48");   // Stronghold of My Fathers: [rohan] possession, fortification; answers a wound to a [rohan] companion
					put("level", "7_92");          // First Level: [gondor] condition, fortification; a maneuver ability only
					put("terrible", "8_47");       // Stronger and More Terrible: [gondor] condition in the support area, no fortification
					put("eye", "12_45");           // Confronting the Eye: Free Peoples condition, "Fellowship: Spot a [gondor] Man and add (2) to..."
					// Seeking Its Master: [sauron] condition, "Shadow: Exert a [sauron] Orc and remove (2) to draw a card."
					put("seeking1", "1_276");
					put("seeking2", "1_276");
					put("seeking3", "1_276");
					// Hollowing of Isengard: [isengard] condition, "Shadow: Discard this condition to add (3)."
					put("hollowing1", "3_54");
					put("hollowing2", "3_54");
					put("hollowing3", "3_54");
					// Ships of Great Draught: [raider] possession, "Shadow: Remove a threat or discard a [raider] card from
					// hand to add a [raider] token here."
					put("ships1", "8_65");
					put("ships2", "8_65");
					// Corsair Helmsman: [raider] minion, vitality 2, "Response: If your [raider] card is about to be hindered
					// or discarded by another card, exert or hinder this minion to prevent that."
					put("helmsman", "103_46");
					put("taskmaster1", "2_90");    // Orc Taskmaster: [sauron] Orc, vitality 3, "Shadow: Exert this minion to add (1)."
					put("taskmaster2", "2_90");
					put("enraged", "1_254");       // Mordor Enraged: [sauron] condition a minion bears, "Shadow: Exert bearer to add (1) for each archer companion."
					// Black Breath: [wraith] condition, "Plays to your support area. Skirmish: Transfer this condition from
					// your support area to a character skirmishing a Nazgul. Burdens and wounds may not be removed from bearer."
					put("breath1", "1_207");
					put("breath2", "1_207");
					put("breath3", "1_207");
					put("lemenya", "1_232");       // Ulaire Lemenya, Lieutenant of Morgul: Nazgul; bearers of [wraith] conditions are strength -2
					// Wolves of Isengard: [isengard] condition, "Plays on a plains site you control. Regroup: Exert an
					// [isengard] Orc to make the Free Peoples player wound a companion."
					put("wolves", "5_69");
					put("forger", "3_56");         // Isengard Forger: [isengard] Orc, vitality 2; a Shadow-phase ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatGateofMinasTirithStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Great Gate of Minas Tirith
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Fortification.  To play, exert a [gondor] companion (or spot 3 knights).
		 * 	Each time the special ability of a Shadow support card is used, you may hinder a fortification to hinder that card.  Then you may exert X knights to hinder X other copies of that card with the same title.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gate");

		assertEquals("Great Gate of Minas Tirith", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedToTheSupportAreaByExertingAGondorCompanionWithOnlyTwoKnightsToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var oath = scn.GetFreepsCard("oath");
		var rohanAragorn = scn.GetFreepsCard("rohanAragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(knight1, knight2, oath, rohanAragorn);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(gate);

		scn.StartGame();
		// The only [gondor] ally there is has 1 vitality and could never exert. He is given a second, so that
		// nothing but his being an ally keeps him out of the choice.
		scn.ApplyAdHocModifier(new VitalityModifier(null, merchant, null, 1));

		assertEquals(0, scn.GetWoundsOn(oath));
		assertTrue(scn.FreepsPlayAvailable(gate));

		scn.FreepsPlayCard(gate);

		// Two knights are one short of three, so a [gondor] companion exerts: a knight or not, a Man or not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1, knight2, oath));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rohanAragorn, merchant, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(oath);

		assertEquals(1, scn.GetWoundsOn(oath));
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertEquals(0, scn.GetWoundsOn(knight2));
		assertInZone(Zone.SUPPORT, gate);
		assertNotAttachedTo(gate, oath);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithEveryGondorCompanionExhaustedUntilAThirdKnightIsPlayedAndThenNobodyExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var knight3 = scn.GetFreepsCard("knight3");
		var oath = scn.GetFreepsCard("oath");
		var rohanAragorn = scn.GetFreepsCard("rohanAragorn");
		scn.MoveCompanionsToTable(knight1, knight2, oath, rohanAragorn);
		// Vitality 3 with 2 wounds: none of the [gondor] companions can exert.
		scn.AddWoundsToChar(knight1, 2);
		scn.AddWoundsToChar(knight2, 2);
		scn.AddWoundsToChar(oath, 2);
		scn.MoveCardsToHand(gate, knight3);

		scn.StartGame();

		// Two knights, and a [rohan] companion who could exert.
		assertFalse(scn.FreepsPlayAvailable(gate));

		scn.FreepsPlayCard(knight3);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(gate));

		scn.FreepsPlayCard(gate);

		// With three knights to spot nobody exerts, though the third knight could.
		assertEquals(0, scn.GetWoundsOn(knight3));
		assertEquals(0, scn.GetWoundsOn(rohanAragorn));
		assertEquals(2, scn.GetWoundsOn(knight1));
		assertInZone(Zone.SUPPORT, gate);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachUseOfAShadowSupportCardsAbilityMayBeAnsweredByHinderingAFortificationToHinderThatCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var stronghold = scn.GetFreepsCard("stronghold");
		var level = scn.GetFreepsCard("level");
		var terrible = scn.GetFreepsCard("terrible");
		var oath = scn.GetFreepsCard("oath");
		var knight3 = scn.GetFreepsCard("knight3");
		var seeking1 = scn.GetShadowCard("seeking1");
		var seeking2 = scn.GetShadowCard("seeking2");
		var taskmaster = scn.GetShadowCard("taskmaster1");
		scn.MoveCompanionsToTable(oath, knight3);
		// The one knight cannot exert.
		scn.AddWoundsToChar(knight3, 2);
		scn.MoveCardsToSupportArea(gate, stronghold, level, terrible, seeking1, seeking2);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetShadowHandCount());
		int twilight = scn.GetTwilight();

		// Seeking Its Master: exert a [sauron] Orc and remove (2) to draw a card.
		scn.ShadowUseCardAction(seeking1);
		assertEquals(1, scn.GetWoundsOn(taskmaster));
		assertEquals(twilight - 2, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gate));

		scn.FreepsDeclineOptionalTrigger();
		assertFalse(scn.IsHindered(gate));
		assertFalse(scn.IsHindered(seeking1));
		assertEquals(1, scn.GetShadowHandCount());

		// The same card used a second time is answered a second time.
		scn.ShadowUseCardAction(seeking1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gate));
		scn.FreepsAcceptOptionalTrigger();

		// Any fortification, the Gate itself among them: a [gondor] condition and a [rohan] possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gate, stronghold, level));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(terrible));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gate);

		assertTrue(scn.IsHindered(gate));
		assertFalse(scn.IsHindered(stronghold));
		assertFalse(scn.IsHindered(level));
		assertTrue(scn.IsHindered(seeking1));
		assertFalse(scn.IsHindered(seeking2));
		// A card hindered while its ability is being carried out still carries it out: the card is drawn.
		assertEquals(2, scn.GetShadowHandCount());

		// There is another copy to hinder, and no knight who can exert: nobody is asked to.
		assertEquals(2, scn.GetWoundsOn(knight3));
		assertEquals(0, scn.GetWoundsOn(oath));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ThenAKnightMayExertToHinderOneOtherCopyOfThePlayersChoosing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var knight3 = scn.GetFreepsCard("knight3");
		var pippin = scn.GetFreepsCard("pippin");
		var oath = scn.GetFreepsCard("oath");
		var seeking1 = scn.GetShadowCard("seeking1");
		var seeking2 = scn.GetShadowCard("seeking2");
		var seeking3 = scn.GetShadowCard("seeking3");
		var hollowing = scn.GetShadowCard("hollowing1");
		var taskmaster = scn.GetShadowCard("taskmaster1");
		scn.MoveCompanionsToTable(knight1, knight2, knight3, pippin, oath);
		// A knight who cannot exert.
		scn.AddWoundsToChar(knight3, 2);
		scn.MoveCardsToSupportArea(gate, seeking1, seeking2, seeking3, hollowing);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowUseCardAction(seeking1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gate));
		scn.FreepsAcceptOptionalTrigger();

		// The Gate is the only fortification, so it is the one hindered. Its own text is carried out to the end
		// all the same.
		assertTrue(scn.IsHindered(gate));
		assertTrue(scn.IsHindered(seeking1));

		// Knights of either race who can exert; not the [gondor] companion who is no knight.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1, knight2, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight3, oath));
		assertEquals(0, scn.FreepsGetChoiceMin());
		// X is the player's: three knights could exert, though there are only two other copies.
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(pippin);

		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertEquals(0, scn.GetWoundsOn(knight2));

		// One knight, one other copy: not the card that was used, nor a Shadow support card of another title.
		assertTrue(scn.FreepsHasCardChoiceAvailable(seeking2, seeking3));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(seeking1, hollowing));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(seeking3);

		assertTrue(scn.IsHindered(seeking3));
		assertFalse(scn.IsHindered(seeking2));
		assertFalse(scn.IsHindered(hollowing));
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardDiscardedToUseItsAbilityStillSetsItOffAndTwoKnightsHinderTwoOtherCopies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var hollowing1 = scn.GetShadowCard("hollowing1");
		var hollowing2 = scn.GetShadowCard("hollowing2");
		var hollowing3 = scn.GetShadowCard("hollowing3");
		scn.MoveCompanionsToTable(knight1, knight2);
		scn.MoveCardsToSupportArea(gate, hollowing1, hollowing2, hollowing3);

		scn.StartGame();
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(hollowing2));

		// Hollowing of Isengard is discarded to add (3).
		scn.ShadowUseCardAction(hollowing1);
		assertInZone(Zone.DISCARD, hollowing1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gate));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.IsHindered(gate));

		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1, knight2));
		scn.FreepsChooseCards(knight1, knight2);

		assertEquals(1, scn.GetWoundsOn(knight1));
		assertEquals(1, scn.GetWoundsOn(knight2));
		assertTrue(scn.IsHindered(hollowing2));
		assertTrue(scn.IsHindered(hollowing3));
		assertInZone(Zone.DISCARD, hollowing1);
		assertEquals(twilight + 3, scn.GetTwilight());
		// A hindered copy no longer offers its ability.
		assertFalse(scn.ShadowActionAvailable(hollowing2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NoMoreKnightsThanCanExertAreAskedAndDecliningLeavesTheOtherCopiesAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight3 = scn.GetFreepsCard("knight3");
		var oath = scn.GetFreepsCard("oath");
		var seeking1 = scn.GetShadowCard("seeking1");
		var seeking2 = scn.GetShadowCard("seeking2");
		var seeking3 = scn.GetShadowCard("seeking3");
		var taskmaster = scn.GetShadowCard("taskmaster1");
		scn.MoveCompanionsToTable(knight1, knight3, oath);
		// A knight who cannot exert, beside one who can and a [gondor] companion who is no knight.
		scn.AddWoundsToChar(knight3, 2);
		scn.MoveCardsToSupportArea(gate, seeking1, seeking2, seeking3);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowUseCardAction(seeking1);
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.IsHindered(seeking1));

		// Two other copies, and one knight who can exert.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight3, oath));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsDeclineChoosing();

		assertEquals(0, scn.GetWoundsOn(knight1));
		assertFalse(scn.IsHindered(seeking2));
		assertFalse(scn.IsHindered(seeking3));
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(seeking2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheCardThatWasUsedIsNotOneOfTheOtherCopiesEvenWhenItsOwnHinderWasPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var ships1 = scn.GetShadowCard("ships1");
		var ships2 = scn.GetShadowCard("ships2");
		var helmsman = scn.GetShadowCard("helmsman");
		scn.MoveCompanionsToTable(knight1, knight2);
		scn.MoveCardsToSupportArea(gate, ships1, ships2);

		scn.StartGame();
		scn.MoveMinionsToTable(helmsman);
		scn.AddThreats(1);
		scn.FreepsPass();

		scn.ShadowUseCardAction(ships1);
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.IsHindered(gate));

		// Corsair Helmsman exerts to keep the [raider] possession from being hindered.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(helmsman));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChoose("Exert");
		assertEquals(1, scn.GetWoundsOn(helmsman));
		assertFalse(scn.IsHindered(ships1));

		// The Gate's cost is paid and its "then" goes on. Both knights could exert; one does.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1, knight2));
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(knight1);
		assertEquals(1, scn.GetWoundsOn(knight1));

		// The card that was used is in play and not hindered, and it is still not one of the "other copies": there
		// is one of those, so it is hindered with no choice asked, and the Helmsman is asked about it at once.

		// The Helmsman is exhausted and could still hinder himself to save the second copy; the Shadow player declines.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(helmsman));
		scn.ShadowDeclineOptionalTrigger();

		assertTrue(scn.IsHindered(ships2));
		assertFalse(scn.IsHindered(ships1));
		assertEquals(0, scn.GetWoundsOn(knight2));
		// The first copy's ability is carried out: its token is added.
		assertEquals(1, scn.GetCultureTokensOn(ships1));
		assertEquals(0, scn.GetCultureTokensOn(ships2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACopyACompanionBearsIsOneOfTheOtherCopiesAsMuchAsOneInTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var breath1 = scn.GetShadowCard("breath1");
		var breath2 = scn.GetShadowCard("breath2");
		var breath3 = scn.GetShadowCard("breath3");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(knight1, knight2);
		scn.MoveCardsToSupportArea(gate, breath1, breath2);
		// A copy that was transferred to a companion in an earlier skirmish.
		scn.AttachCardsTo(knight2, breath3);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight1, lemenya);
		scn.FreepsResolveSkirmish(knight1);
		scn.FreepsPass();

		// Black Breath is used from the support area, in the skirmish phase.
		scn.ShadowUseCardAction(breath1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gate));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.IsHindered(gate));
		assertTrue(scn.IsHindered(breath1));

		// Two knights who can exert, and two other copies.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1, knight2));
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(knight2);
		assertEquals(1, scn.GetWoundsOn(knight2));

		// "Other copies of that card": the text does not say where they are. One is in the support area and one
		// is on a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(breath2, breath3));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(breath1));
		scn.FreepsChooseCard(breath3);

		assertTrue(scn.IsHindered(breath3));
		assertAttachedTo(breath3, knight2);
		assertFalse(scn.IsHindered(breath2));
		assertInZone(Zone.SUPPORT, breath2);
		// The ability that was used is carried out to the end on the card hindered meanwhile: it is transferred
		// to the companion in the skirmish.
		assertAttachedTo(breath1, knight1);
		assertTrue(scn.IsHindered(breath1));
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedForAFreePeoplesSupportCardNorAMinionsAbilityNorACardAMinionBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var eye = scn.GetFreepsCard("eye");
		var knight1 = scn.GetFreepsCard("knight1");
		var seeking = scn.GetShadowCard("seeking1");
		var taskmaster1 = scn.GetShadowCard("taskmaster1");
		var taskmaster2 = scn.GetShadowCard("taskmaster2");
		var enraged = scn.GetShadowCard("enraged");
		scn.MoveCompanionsToTable(knight1);
		scn.MoveCardsToSupportArea(gate, eye, seeking);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster1, taskmaster2);
		scn.AttachCardsTo(taskmaster2, enraged);
		scn.FreepsRefreshDecision();
		scn.SetTwilight(10);

		// A Free Peoples condition in the support area: Confronting the Eye adds (2).
		scn.FreepsUseCardAction(eye);
		assertEquals(12, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();
		int twilight = scn.GetTwilight();

		// A minion's own ability: Orc Taskmaster exerts to add (1).
		scn.ShadowUseCardAction(taskmaster1);
		assertEquals(twilight + 1, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A Shadow condition that is on a minion and not in the support area: Mordor Enraged exerts its bearer.
		scn.ShadowUseCardAction(enraged);
		assertEquals(1, scn.GetWoundsOn(taskmaster2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The same kind of card in the support area.
		scn.ShadowUseCardAction(seeking);
		scn.ShadowChooseCard(taskmaster1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gate));

		scn.FreepsDeclineOptionalTrigger();
		assertFalse(scn.IsHindered(gate));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AShadowCardBorneByASiteTheShadowPlayerControlsIsASupportCardWhoseAbilitySetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gate = scn.GetFreepsCard("gate");
		var aragorn = scn.GetFreepsCard("rohanAragorn");
		var wolves = scn.GetShadowCard("wolves");
		var forger = scn.GetShadowCard("forger");

		scn.StartGame();
		// Both players have passed Ettenmoors, site 2 and a plains site, so it can be controlled.
		scn.SkipToSite(3);
		var ettenmoors = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(ettenmoors);
		// A controlled site lies in its controller's support area (CR 4.2 "control"), and so does what it bears.
		assertInZone(Zone.SUPPORT, ettenmoors);
		scn.AttachCardsTo(ettenmoors, wolves);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gate);
		scn.MoveMinionsToTable(forger);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(forger));
		assertTrue(scn.ShadowActionAvailable(wolves));

		// Wolves of Isengard: exert an [isengard] Orc to make the Free Peoples player wound a companion.
		scn.ShadowUseCardAction(wolves);
		assertEquals(1, scn.GetWoundsOn(forger));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gate));
		scn.FreepsAcceptOptionalTrigger();

		// The Gate is the one fortification and no knight is in play: nothing more is asked about the hinder.
		assertTrue(scn.IsHindered(gate));
		assertTrue(scn.IsHindered(wolves));
		assertAttachedTo(wolves, ettenmoors);

		// The ability is carried out all the same.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AShadowPossessionInTheSupportAreaSetsItOffTooAndWithNoOtherCopyTheKnightIsStillAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var ships = scn.GetShadowCard("ships1");
		scn.MoveCompanionsToTable(knight1);
		scn.MoveCardsToSupportArea(gate, ships);

		scn.StartGame();
		scn.AddThreats(1);
		scn.FreepsPass();

		assertEquals(0, scn.GetCultureTokensOn(ships));

		// Ships of Great Draught: remove a threat to add a [raider] token here.
		scn.ShadowUseCardAction(ships);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gate));
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.IsHindered(gate));
		assertTrue(scn.IsHindered(ships));

		// No other Ships of Great Draught is in play, and the knight is asked all the same: the exertion is the
		// player's to make, whatever it buys. He stays as he is.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsDeclineChoosing();
		assertEquals(0, scn.GetWoundsOn(knight1));

		// The ability is carried out on the card that has just been hindered, but a token cannot be added to a
		// hindered card: the threat was paid for nothing.
		assertEquals(0, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsOfferedOnlyOnceForOneUseWhenAnotherFortificationIsTheOneHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var stronghold = scn.GetFreepsCard("stronghold");
		var oath = scn.GetFreepsCard("oath");
		var seeking = scn.GetShadowCard("seeking1");
		var taskmaster = scn.GetShadowCard("taskmaster1");
		scn.MoveCompanionsToTable(oath);
		scn.MoveCardsToSupportArea(gate, stronghold, seeking);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowUseCardAction(seeking);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gate));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(stronghold);

		assertTrue(scn.IsHindered(stronghold));
		assertFalse(scn.IsHindered(gate));
		assertTrue(scn.IsHindered(seeking));

		// One use of the ability, one offer: the card is drawn and the Shadow player goes on.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void KnightsMayExertWhenThereIsNoOtherCopyToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var pippin = scn.GetFreepsCard("pippin");
		var seeking = scn.GetShadowCard("seeking1");
		var taskmaster = scn.GetShadowCard("taskmaster1");
		scn.MoveCompanionsToTable(knight1, pippin);
		scn.MoveCardsToSupportArea(gate, seeking);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowUseCardAction(seeking);
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.IsHindered(gate));
		assertTrue(scn.IsHindered(seeking));

		// X is the player's: both knights could exert, whatever there is to hinder.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1, pippin));
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(pippin);

		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void KnightsExertedBeyondTheOtherCopiesThereAreHinderNothingMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var seeking1 = scn.GetShadowCard("seeking1");
		var seeking2 = scn.GetShadowCard("seeking2");
		var hollowing = scn.GetShadowCard("hollowing1");
		var taskmaster = scn.GetShadowCard("taskmaster1");
		scn.MoveCompanionsToTable(knight1, knight2);
		scn.MoveCardsToSupportArea(gate, seeking1, seeking2, hollowing);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowUseCardAction(seeking1);
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.IsHindered(gate));
		assertTrue(scn.IsHindered(seeking1));

		// Two knights exert and there is one other copy.
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(knight1, knight2);
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertEquals(1, scn.GetWoundsOn(knight2));

		// The one other copy is hindered with no choice asked; the second exertion hinders nothing, and a Shadow
		// support card of another title is left alone.
		assertTrue(scn.IsHindered(seeking2));
		assertFalse(scn.IsHindered(hollowing));
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
